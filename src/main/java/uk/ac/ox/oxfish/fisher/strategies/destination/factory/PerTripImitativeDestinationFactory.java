/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.fisher.strategies.destination.factory;


import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.selfanalysis.ObjectiveFunction;
import uk.ac.ox.oxfish.fisher.selfanalysis.factory.HourlyProfitObjectiveFactory;
import uk.ac.ox.oxfish.fisher.strategies.destination.ExploreExploitImitateDestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.FavoriteDestinationStrategy;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.Gatherer;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.adaptation.maximization.BeamHillClimbing;
import uk.ac.ox.oxfish.utility.adaptation.maximization.DefaultBeamHillClimbing;
import uk.ac.ox.oxfish.utility.adaptation.probability.AdaptationProbability;
import uk.ac.ox.oxfish.utility.adaptation.probability.factory.ExplorationPenaltyProbabilityFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.UniformDoubleParameter;

import java.util.function.Predicate;

import static uk.ac.ox.oxfish.fisher.strategies.destination.ExploreExploitImitateDestinationStrategy.*;

/**
 * creates a trip strategy that has imitates friends when not exploring
 */
public class PerTripImitativeDestinationFactory implements AlgorithmFactory<ExploreExploitImitateDestinationStrategy>
{

    private AlgorithmFactory<? extends ObjectiveFunction<Fisher>> objectiveFunction =
            new HourlyProfitObjectiveFactory();

    private DoubleParameter stepSize = new UniformDoubleParameter(1d, 10d);


    private AlgorithmFactory<? extends AdaptationProbability> probability =
            new ExplorationPenaltyProbabilityFactory(.2,
                                                     1d,
                                                     .02,
                                                     .01);

    private boolean ignoreEdgeDirection = true;

    /**
     * If you imitate a friend and it makes you worse off then as long as this is a positive number that's by how much
     * the utility needs to drop in imitating a friend in order for you to unfriend him. If this is negative or null
     * then no unfriending ever happens
     */
    private DoubleParameter dropInUtilityNeededForUnfriend = new FixedDoubleParameter(-1);


    /**
     * whether, when imitating, you ask your friend who is doing best (true) or a friend at random (false)
     */
    private boolean alwaysCopyBest = BeamHillClimbing.DEFAULT_ALWAYS_COPY_BEST;


    private boolean backtracksOnBadExploration = BeamHillClimbing.DEFAULT_BACKTRACKS_ON_BAD_EXPLORATION;


    /**
     * if this is true, the exploration tries hard to avoid protected areas.
     */
    private boolean automaticallyIgnoreMPAs = false;

    /**
     * areas where fishing just will never work are not tried when this flag is set to true
     */
    private boolean automaticallyIgnoreAreasWhereFishNeverGrows = false;


    private boolean ignoreFailedTrips = false;



    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public ExploreExploitImitateDestinationStrategy apply(FishState state) {

        MersenneTwisterFast random = state.random;
        NauticalMap map = state.getMap();
        double probabilityUnfriending = dropInUtilityNeededForUnfriend.apply(state.getRandom());
        DefaultBeamHillClimbing algorithm;

        Predicate<SeaTile>  explorationValidator = automaticallyIgnoreMPAs ?
                new Predicate<SeaTile>() {
                    @Override
                    public boolean test(SeaTile tile) {
                        return !tile.isProtected();
                    }
                } :
                new Predicate<SeaTile>() {
                    @Override
                    public boolean test(SeaTile tile) {
                        return true;
                    }
                };

        if(automaticallyIgnoreAreasWhereFishNeverGrows) {
            final Predicate<SeaTile> previosValidator = explorationValidator;
            explorationValidator = new Predicate<SeaTile>() {
                @Override
                public boolean test(SeaTile seaTile) {
                    return
                            previosValidator.test(seaTile) &&
                                    seaTile.isFishingEvenPossibleHere();
                }
            };
        }
        if(probabilityUnfriending <= 0)
        { //no unfriending

            algorithm = new DefaultBeamHillClimbing(alwaysCopyBest,
                                                    BeamHillClimbing.DEFAULT_DYNAMIC_NETWORK,
                                                    stepSize.apply(
                                                            random).intValue(),
                                                    100, backtracksOnBadExploration);
        }
        else {
            algorithm = DefaultBeamHillClimbing.BeamHillClimbingWithUnfriending(alwaysCopyBest,
                                                                                probabilityUnfriending,
                                                                                stepSize.apply(
                                                                                        random).intValue(),
                                                                                100);
        }

        //never start from an invalid spot!
        SeaTile initialFavoriteSpot;
        do {
            initialFavoriteSpot = map.getRandomBelowWaterLineSeaTile(random);
        }
        while (!explorationValidator.test(initialFavoriteSpot));



        //add gatherer if necessary
        if(state.getYearlyDataSet().getColumn(EXPLORING_COLUMN_NAME)==null)
        {
            registerGatherer(state, EXPLORING_COLUMN_NAME);
            registerGatherer(state, EXPLOITING_COLUMN_NAME);
            registerGatherer(state, IMITATING_COLUMN_NAME);
        }

        return new ExploreExploitImitateDestinationStrategy(
                new FavoriteDestinationStrategy(initialFavoriteSpot), algorithm,
                probability.apply(state),
                objectiveFunction.apply(state), explorationValidator,
                ignoreFailedTrips);


    }

    private void registerGatherer(FishState state, final String exploringColumnName) {
        state.getYearlyDataSet().registerGatherer(exploringColumnName,
                                                  new Gatherer<FishState>() {
                                                      @Override
                                                      public Double apply(FishState fishState) {
                                                          double sum =0;
                                                          for(Fisher fisher : fishState.getFishers())
                                                          {
                                                              if(fisher.getYearlyCounter().hasColumn(
                                                                      exploringColumnName))
                                                                  sum+=fisher.getYearlyCounter().getColumn(
                                                                          exploringColumnName);
                                                          }
                                                          return sum;
                                                      }
                                                  }, Double.NaN);
    }

    public DoubleParameter getStepSize() {
        return stepSize;
    }

    public void setStepSize(DoubleParameter stepSize) {
        this.stepSize = stepSize;
    }

    public boolean isIgnoreEdgeDirection() {
        return ignoreEdgeDirection;
    }

    public void setIgnoreEdgeDirection(boolean ignoreEdgeDirection) {
        this.ignoreEdgeDirection = ignoreEdgeDirection;
    }

    public AlgorithmFactory<? extends AdaptationProbability> getProbability() {
        return probability;
    }



    public void setProbability(
            AlgorithmFactory<? extends AdaptationProbability> probability) {
        this.probability = probability;
    }


    public DoubleParameter getDropInUtilityNeededForUnfriend() {
        return dropInUtilityNeededForUnfriend;
    }

    public void setDropInUtilityNeededForUnfriend(
            DoubleParameter dropInUtilityNeededForUnfriend) {
        this.dropInUtilityNeededForUnfriend = dropInUtilityNeededForUnfriend;
    }

    public boolean isAlwaysCopyBest() {
        return alwaysCopyBest;
    }

    public void setAlwaysCopyBest(boolean alwaysCopyBest) {
        this.alwaysCopyBest = alwaysCopyBest;
    }


    /**
     * Getter for property 'objectiveFunction'.
     *
     * @return Value for property 'objectiveFunction'.
     */
    public AlgorithmFactory<? extends ObjectiveFunction<Fisher>> getObjectiveFunction() {
        return objectiveFunction;
    }

    /**
     * Setter for property 'objectiveFunction'.
     *
     * @param objectiveFunction Value to set for property 'objectiveFunction'.
     */
    public void setObjectiveFunction(
            AlgorithmFactory<? extends ObjectiveFunction<Fisher>> objectiveFunction) {
        this.objectiveFunction = objectiveFunction;
    }


    /**
     * Getter for property 'backtracksOnBadExploration'.
     *
     * @return Value for property 'backtracksOnBadExploration'.
     */
    public boolean isBacktracksOnBadExploration() {
        return backtracksOnBadExploration;
    }

    /**
     * Setter for property 'backtracksOnBadExploration'.
     *
     * @param backtracksOnBadExploration Value to set for property 'backtracksOnBadExploration'.
     */
    public void setBacktracksOnBadExploration(boolean backtracksOnBadExploration) {
        this.backtracksOnBadExploration = backtracksOnBadExploration;
    }

    /**
     * Getter for property 'automaticallyIgnoreMPAs'.
     *
     * @return Value for property 'automaticallyIgnoreMPAs'.
     */
    public boolean isAutomaticallyIgnoreMPAs() {
        return automaticallyIgnoreMPAs;
    }

    /**
     * Setter for property 'automaticallyIgnoreMPAs'.
     *
     * @param automaticallyIgnoreMPAs Value to set for property 'automaticallyIgnoreMPAs'.
     */
    public void setAutomaticallyIgnoreMPAs(boolean automaticallyIgnoreMPAs) {
        this.automaticallyIgnoreMPAs = automaticallyIgnoreMPAs;
    }

    /**
     * Getter for property 'automaticallyIgnoreAreasWhereFishNeverGrows'.
     *
     * @return Value for property 'automaticallyIgnoreAreasWhereFishNeverGrows'.
     */
    public boolean isAutomaticallyIgnoreAreasWhereFishNeverGrows() {
        return automaticallyIgnoreAreasWhereFishNeverGrows;
    }

    /**
     * Setter for property 'automaticallyIgnoreAreasWhereFishNeverGrows'.
     *
     * @param automaticallyIgnoreAreasWhereFishNeverGrows Value to set for property 'automaticallyIgnoreAreasWhereFishNeverGrows'.
     */
    public void setAutomaticallyIgnoreAreasWhereFishNeverGrows(boolean automaticallyIgnoreAreasWhereFishNeverGrows) {
        this.automaticallyIgnoreAreasWhereFishNeverGrows = automaticallyIgnoreAreasWhereFishNeverGrows;
    }


    /**
     * Getter for property 'ignoreFailedTrips'.
     *
     * @return Value for property 'ignoreFailedTrips'.
     */
    public boolean isIgnoreFailedTrips() {
        return ignoreFailedTrips;
    }

    /**
     * Setter for property 'ignoreFailedTrips'.
     *
     * @param ignoreFailedTrips Value to set for property 'ignoreFailedTrips'.
     */
    public void setIgnoreFailedTrips(boolean ignoreFailedTrips) {
        this.ignoreFailedTrips = ignoreFailedTrips;
    }
}
