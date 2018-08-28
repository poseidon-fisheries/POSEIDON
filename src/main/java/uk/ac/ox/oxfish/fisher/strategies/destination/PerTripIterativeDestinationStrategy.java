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

package uk.ac.ox.oxfish.fisher.strategies.destination;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.Action;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.fisher.selfanalysis.HourlyProfitInTripObjective;
import uk.ac.ox.oxfish.fisher.selfanalysis.ObjectiveFunction;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.adaptation.Actuator;
import uk.ac.ox.oxfish.utility.adaptation.Adaptation;
import uk.ac.ox.oxfish.utility.adaptation.ExploreImitateAdaptation;
import uk.ac.ox.oxfish.utility.adaptation.Sensor;
import uk.ac.ox.oxfish.utility.adaptation.maximization.AdaptationAlgorithm;
import uk.ac.ox.oxfish.utility.adaptation.probability.AdaptationProbability;
import uk.ac.ox.oxfish.utility.adaptation.probability.FixedProbability;

import java.util.function.Predicate;

/**
 * Like the YearlyIterativeDestinationStrategy except that rather than doing it every
 * year this is done every trip <br>
 *     In terms of code this strategy doesn't actually step but instead listen to the fisher for
 *     new trips
 * Created by carrknight on 6/19/15.
 */
public class PerTripIterativeDestinationStrategy implements DestinationStrategy {



    private final Adaptation<SeaTile> algorithm;

    /**
     * this strategy works by modifying the "favorite" destination of its delegate
     */
    private final FavoriteDestinationStrategy delegate;

    /**
     * fisher I am listening to
     */
    private Fisher fisher;




    public PerTripIterativeDestinationStrategy(
            FavoriteDestinationStrategy delegate,
            AdaptationAlgorithm<SeaTile> algorithm,
            AdaptationProbability probability,
            final ObjectiveFunction<Fisher> objective,
            final Predicate<SeaTile> explorationValidator,
            boolean ignoreFailedTrips)
    {
        this.delegate = delegate;
        this.algorithm = new ExploreImitateAdaptation<SeaTile>(
                new Predicate<Fisher>() {
                    @Override
                    public boolean test(Fisher fisher) {
                        return !(ignoreFailedTrips && fisher.getLastFinishedTrip().isCutShort());
                    }
                },


                algorithm,
                new Actuator<Fisher,SeaTile>() {
                    @Override
                    public void apply(Fisher fisher, SeaTile change, FishState model) {
                        if (change.getAltitude() < 0) //ignores "go to land" commands
                            delegate.setFavoriteSpot(change);
                    }
                },
                new Sensor<Fisher,SeaTile>() {
                    @Override
                    public SeaTile scan(Fisher fisher1) {
                        TripRecord trip = fisher1.getLastFinishedTrip();
                        if (trip == null || !trip.isCompleted() ||
                                trip.getTilesFished().isEmpty())
                            if(fisher1==fisher)
                                return delegate.getFavoriteSpot();
                            else
                                return null;
                        else
                            return trip.getMostFishedTileInTrip();
                    }
                },
                objective,
                probability, explorationValidator
        );

    }


    public PerTripIterativeDestinationStrategy(
            FavoriteDestinationStrategy delegate,
            Adaptation<SeaTile> adaptation)
    {
        this.delegate = delegate;
        this.algorithm = adaptation;

    }

    public PerTripIterativeDestinationStrategy(
            final FavoriteDestinationStrategy delegate,
            AdaptationAlgorithm<SeaTile> algorithm,
            double randomizationProbability,
            double imitationProbability, final HourlyProfitInTripObjective objective,
            final Predicate<SeaTile> explorationValidator) {
        this(delegate, algorithm, new FixedProbability(randomizationProbability,imitationProbability),
             objective, explorationValidator, false);
    }



    /**
     * tell the startable to turnoff,
     * @param fisher
     */
    @Override
    public void turnOff(Fisher fisher) {
        delegate.turnOff(fisher);
        fisher.removePerTripAdaptation(algorithm);

    }

    /**
     * starts a per-trip adaptation
     */
    @Override
    public void start(FishState model, Fisher fisher) {
        delegate.start(model,fisher);


        this.fisher=fisher;
        fisher.addPerTripAdaptation(algorithm);
    }



    /**
     * decides where to go.
     *
     * @param fisher
     * @param random        the randomizer. It probably comes from the fisher but I make explicit it might be needed
     * @param model         the model link
     * @param currentAction what action is the fisher currently taking that prompted to check for destination   @return the destination
     */
    @Override
    public SeaTile chooseDestination(
            Fisher fisher, MersenneTwisterFast random,
            FishState model,
            Action currentAction) {
        return delegate.chooseDestination(fisher, random, model, currentAction);
    }


    /**
     * Getter for property 'delegate'.
     *
     * @return Value for property 'delegate'.
     */
    public FavoriteDestinationStrategy getDelegate() {
        return delegate;
    }

    public Adaptation<SeaTile> getAlgorithm() {
        return algorithm;
    }


    public SeaTile getFavoriteSpot() {
        return delegate.getFavoriteSpot();
    }


    public void forceFavoriteSpot(SeaTile newFavoriteSpot){
        delegate.setFavoriteSpot(newFavoriteSpot);
    }
}
