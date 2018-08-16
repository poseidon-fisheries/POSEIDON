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

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.selfanalysis.ObjectiveFunction;
import uk.ac.ox.oxfish.fisher.selfanalysis.factory.HourlyProfitObjectiveFactory;
import uk.ac.ox.oxfish.fisher.strategies.destination.BanditDestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.FavoriteDestinationStrategy;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretization;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretizer;
import uk.ac.ox.oxfish.geography.discretization.SquaresMapDiscretizerFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.Averager;
import uk.ac.ox.oxfish.model.data.factory.ExponentialMovingAverageFactory;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.Locker;
import uk.ac.ox.oxfish.utility.bandit.BanditAverage;
import uk.ac.ox.oxfish.utility.bandit.factory.BanditSupplier;
import uk.ac.ox.oxfish.utility.bandit.factory.EpsilonGreedyBanditFactory;

/**
 * Created by carrknight on 11/10/16.
 */
public class BanditDestinationFactory implements AlgorithmFactory<BanditDestinationStrategy>{



    private static Locker<FishState,MapDiscretization> locker = new Locker();

    private AlgorithmFactory<? extends Averager> average = new ExponentialMovingAverageFactory();

    private AlgorithmFactory<? extends BanditSupplier> bandit = new EpsilonGreedyBanditFactory();


    private AlgorithmFactory<? extends MapDiscretizer> discretizer = new SquaresMapDiscretizerFactory();

    private AlgorithmFactory<? extends ObjectiveFunction<Fisher>> objectiveFunction =
            new HourlyProfitObjectiveFactory(true);



    private boolean automaticallyIgnoreMPAs = true;

    private boolean automaticallyIgnoreWastelands = true;

    private boolean imitate = false;


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public BanditDestinationStrategy apply(FishState state)
    {

        MapDiscretization map =  locker.
                presentKey(state,
                           () -> {
                               MapDiscretization discretization =
                                       new MapDiscretization(
                                               discretizer.apply(state));
                               discretization.discretize(state.getMap());
                               return discretization;
                           }
                );


        BanditDestinationStrategy banditDestinationStrategy = new BanditDestinationStrategy(
                arms -> new BanditAverage(
                        arms,
                        average,
                        state
                ),
                bandit.apply(state),
                map,
                new FavoriteDestinationStrategy(state.getMap(), state.getRandom()),
                objectiveFunction.apply(state),
                automaticallyIgnoreMPAs, automaticallyIgnoreWastelands);
        banditDestinationStrategy.setImitate(imitate);
        return banditDestinationStrategy;
    }



    /**
     * Getter for property 'average'.
     *
     * @return Value for property 'average'.
     */
    public AlgorithmFactory<? extends Averager> getAverage() {
        return average;
    }

    /**
     * Setter for property 'average'.
     *
     * @param average Value to set for property 'average'.
     */
    public void setAverage(AlgorithmFactory<? extends Averager> average) {
        this.average = average;
    }

    /**
     * Getter for property 'bandit'.
     *
     * @return Value for property 'bandit'.
     */
    public AlgorithmFactory<? extends BanditSupplier> getBandit() {
        return bandit;
    }

    /**
     * Setter for property 'bandit'.
     *
     * @param bandit Value to set for property 'bandit'.
     */
    public void setBandit(
            AlgorithmFactory<? extends BanditSupplier> bandit) {
        this.bandit = bandit;
    }

    /**
     * Getter for property 'discretizer'.
     *
     * @return Value for property 'discretizer'.
     */
    public AlgorithmFactory<? extends MapDiscretizer> getDiscretizer() {
        return discretizer;
    }

    /**
     * Setter for property 'discretizer'.
     *
     * @param discretizer Value to set for property 'discretizer'.
     */
    public void setDiscretizer(
            AlgorithmFactory<? extends MapDiscretizer> discretizer) {
        this.discretizer = discretizer;
    }

    /**
     * Getter for property 'respectMPA'.
     *
     * @return Value for property 'respectMPA'.
     */
    public boolean isAutomaticallyIgnoreMPAs() {
        return automaticallyIgnoreMPAs;
    }

    /**
     * Setter for property 'respectMPA'.
     *
     * @param automaticallyIgnoreMPAs Value to set for property 'respectMPA'.
     */
    public void setAutomaticallyIgnoreMPAs(boolean automaticallyIgnoreMPAs) {
        this.automaticallyIgnoreMPAs = automaticallyIgnoreMPAs;
    }

    /**
     * Getter for property 'automaticallyIgnoreWastelands'.
     *
     * @return Value for property 'automaticallyIgnoreWastelands'.
     */
    public boolean isAutomaticallyIgnoreWastelands() {
        return automaticallyIgnoreWastelands;
    }

    /**
     * Setter for property 'automaticallyIgnoreWastelands'.
     *
     * @param automaticallyIgnoreWastelands Value to set for property 'automaticallyIgnoreWastelands'.
     */
    public void setAutomaticallyIgnoreWastelands(boolean automaticallyIgnoreWastelands) {
        this.automaticallyIgnoreWastelands = automaticallyIgnoreWastelands;
    }

    /**
     * Getter for property 'imitate'.
     *
     * @return Value for property 'imitate'.
     */
    public boolean isImitate() {
        return imitate;
    }

    /**
     * Setter for property 'imitate'.
     *
     * @param imitate Value to set for property 'imitate'.
     */
    public void setImitate(boolean imitate) {
        this.imitate = imitate;
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
}
