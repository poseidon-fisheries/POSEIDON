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

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.Action;
import uk.ac.ox.oxfish.fisher.heatmap.acquisition.AcquisitionFunction;
import uk.ac.ox.oxfish.fisher.heatmap.regression.ErrorTrackingRegression;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.GeographicalRegression;
import uk.ac.ox.oxfish.fisher.log.TripListener;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.fisher.selfanalysis.ObjectiveFunction;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.adaptation.maximization.DefaultBeamHillClimbing;
import uk.ac.ox.oxfish.utility.adaptation.maximization.RandomStep;
import uk.ac.ox.oxfish.utility.adaptation.probability.AdaptationProbability;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * A destination strategy that keeps a heatmap of profits and uses it to guide decisions
 * Created by carrknight on 6/29/16.
 */
public abstract class AbstractHeatmapDestinationStrategy<V> implements DestinationStrategy, TripListener {

    private static final long serialVersionUID = -2481861369768173194L;
    final ObjectiveFunction<Fisher> objectiveFunction;
    /**
     * should we ignore trips that were cut short?
     */
    private final boolean ignoreFailedTrips;
    /**
     * the probability of exploring (shocking the best) and exploiting
     */
    private final AdaptationProbability probability;
    /**
     * what is doing the navigation
     */
    private final FavoriteDestinationStrategy delegate;
    /**
     * what t odo in the case of an exploration
     */
    private final RandomStep<SeaTile> explorationStep;
    Fisher fisher;
    /**
     * geographical regression to learn and predict where you make most money
     */
    private ErrorTrackingRegression<V> heatmap;
    /**
     * the strategy used to scan the profit regression to look for the "best"
     */
    private AcquisitionFunction acquisition;
    private FishState model;
    /**
     * you also listen to your friends trips, here are the listeners to turn off
     */
    private HashMap<Fisher, TripRecord> lastFriendTripRecorded;

    public AbstractHeatmapDestinationStrategy(
        final GeographicalRegression<V> heatmap,
        final AcquisitionFunction acquisition, final boolean ignoreFailedTrips,
        final AdaptationProbability probability,
        final NauticalMap map,
        final MersenneTwisterFast random,
        final int stepSize,
        final ObjectiveFunction<Fisher> objectiveFunction
    ) {
        this.heatmap = new ErrorTrackingRegression<>(heatmap);
        this.acquisition = acquisition;
        this.ignoreFailedTrips = ignoreFailedTrips;
        this.probability = probability;
        this.objectiveFunction = objectiveFunction;
        this.delegate = new FavoriteDestinationStrategy(map, random);
        this.explorationStep = DefaultBeamHillClimbing.DEFAULT_RANDOM_STEP(stepSize, 5);
    }

    @Override
    public void start(final FishState model, final Fisher fisher) {
        this.fisher = fisher;
        this.model = model;

        if (fisher.getDirectedFriends() != null)
            lastFriendTripRecorded = new HashMap<>(fisher.getDirectedFriends().size());
        else
            lastFriendTripRecorded = new HashMap<>(0);
        fisher.addTripListener(this);
        heatmap.start(model, fisher);
    }

    @Override
    public void turnOff(final Fisher fisher) {
        this.fisher.removeTripListener(this);
        lastFriendTripRecorded.clear();
        this.fisher = null;
        model = null;
        heatmap.turnOff(fisher);
        heatmap = null;
    }

    @Override
    public void reactToFinishedTrip(final TripRecord record, final Fisher fisher) {
        SeaTile tile = record.getMostFishedTileInTrip();
        if (tile != null)
            if (!record.isCutShort() || ignoreFailedTrips)
                learnFromTripRecord(record, tile, this.fisher, model);

        //go through your friends and add their observations if they are new
        // (with imitation probability)
        if (this.fisher.getDirectedFriends() != null)
            for (final Fisher friend : this.fisher.getDirectedFriends()) {
                final TripRecord friendTrip = friend.getLastFinishedTrip();
                //if you have already been through this don't worry
                if (lastFriendTripRecorded.get(friend) == friendTrip)
                    continue;
                lastFriendTripRecorded.put(friend, friendTrip);
                tile = friendTrip.getMostFishedTileInTrip();
                if (tile != null)

                    if (!record.isCutShort() || ignoreFailedTrips)
                        if (model.getRandom().nextDouble() <= probability.getImitationProbability())
                            learnFromTripRecord(friendTrip, tile, friend, model);

            }

        //find the optimal
        SeaTile optimal = acquisition.pick(model.getMap(), heatmap, model, this.fisher, delegate.getFavoriteSpot());
        Preconditions.checkState(optimal.isWater());
        if (model.getRandom().nextDouble() <= probability.getExplorationProbability()) {
            optimal = explorationStep.randomStep(model, model.getRandom(), this.fisher, optimal);
            Preconditions.checkState(optimal.isWater());
        }
        delegate.setFavoriteSpot(optimal);
    }

    abstract void learnFromTripRecord(
        final TripRecord record,
        final SeaTile mostFishedTile,
        final Fisher fisherThatMadeTheTrip,
        final FishState model
    );

    public GeographicalRegression<V> getHeatmap() {
        return heatmap;
    }

    /**
     * Setter for property 'heatmap'.
     *
     * @param heatmap Value to set for property 'heatmap'.
     */
    public void setHeatmap(final GeographicalRegression<V> heatmap) {
        this.heatmap = new ErrorTrackingRegression<>(heatmap);
    }

    public AdaptationProbability getProbability() {
        return probability;
    }

    public HashMap<Fisher, TripRecord> getLastFriendTripRecorded() {
        return lastFriendTripRecorded;
    }

    public RandomStep<SeaTile> getExplorationStep() {
        return explorationStep;
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
        final Fisher fisher, final MersenneTwisterFast random, final FishState model, final Action currentAction
    ) {
        return delegate.chooseDestination(fisher, random, model, currentAction);
    }

    public SeaTile getFavoriteSpot() {
        return delegate.getFavoriteSpot();
    }

    /**
     * Getter for property 'acquisition'.
     *
     * @return Value for property 'acquisition'.
     */
    public AcquisitionFunction getAcquisition() {
        return acquisition;
    }

    /**
     * Setter for property 'acquisition'.
     *
     * @param acquisition Value to set for property 'acquisition'.
     */
    public void setAcquisition(final AcquisitionFunction acquisition) {
        this.acquisition = acquisition;
    }

    /**
     * returns list of errors. No protection here, be careful
     */
    public LinkedList<Double> getErrors() {
        return heatmap.getErrors();
    }

    /**
     * Getter for property 'latestError'.
     *
     * @return Value for property 'latestError'.
     */
    public double getLatestError() {
        return heatmap.getLatestError();
    }
}
