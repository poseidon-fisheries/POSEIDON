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

package uk.ac.ox.oxfish.fisher;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.fisher.erotetic.FeatureExtractor;
import uk.ac.ox.oxfish.fisher.erotetic.FeatureExtractors;
import uk.ac.ox.oxfish.fisher.log.*;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.FisherStartable;
import uk.ac.ox.oxfish.model.data.collectors.*;

import java.io.Serializable;
import java.util.*;

public class FisherMemory implements Serializable, FisherStartable {
    /**
     * the data gatherer that fires once a year
     */
    final FisherYearlyTimeSeries yearlyTimeSeries;
    final LocationMemories<TripRecord> tripMemories;
    final List<SharedTripRecord> sharedTrips;
    /**
     * stores trip information
     */
    final TripLogger tripLogger = new TripLogger();
    final private Counter yearlyCounter;
    FisherDailyCounter dailyCounter;
    /**
     * the data gatherer that fires every day
     */
    private FisherDailyTimeSeries dailyTimeSeries;
    private DiscretizedLocationMemory discretizedLocationMemory;
    /**
     * any other thing I want the fisher to remember I will have to store in this very general object
     */
    private HashMap<String, Object> database = new HashMap<>();
    /**
     * an object to extract from seatiles a feature
     */
    private FeatureExtractors<SeaTile> tileRepresentation = new FeatureExtractors<>();

    public FisherMemory() {
        this(
            new LocationMemories<>(1, 3000, 2));
    }

    public FisherMemory(
        LocationMemories<TripRecord> tripMemories
    ) {
        yearlyTimeSeries = new FisherYearlyTimeSeries();
        yearlyCounter = new Counter(IntervalPolicy.EVERY_YEAR);
        this.dailyTimeSeries = new FisherDailyTimeSeries();
        this.tripMemories = tripMemories;
        this.sharedTrips = new ArrayList<SharedTripRecord>();
    }

    public FisherYearlyTimeSeries getYearlyTimeSeries() {
        return yearlyTimeSeries;
    }

    public FisherDailyTimeSeries getDailyTimeSeries() {
        return dailyTimeSeries;
    }

    public Counter getYearlyCounter() {
        return yearlyCounter;
    }

    public FisherDailyCounter getDailyCounter() {
        return dailyCounter;
    }

    public void setDailyCounter(FisherDailyCounter dailyCounter) {
        this.dailyCounter = dailyCounter;
    }

    public List<SharedTripRecord> getSharedTrips() {
        return sharedTrips;
    }

    //At this point, we will assume they are friends
    public List<SharedTripRecord> getTripsSharedWith(Fisher friend) {
        List<SharedTripRecord> sharedTripList = new ArrayList<SharedTripRecord>();
        for (SharedTripRecord sharedTrip : sharedTrips) {
            if (sharedTrip.sharedWithAll() || sharedTrip.getSharedFriends().contains(friend)) {
                sharedTripList.add(sharedTrip);
            }
        }
        return sharedTripList;
    }

    public void shareTrip(TripRecord trip, boolean allFriends, Collection<Fisher> sharedFriends) {
        //if it has been shared before, update allFriends or add to the sharedFriends collection
        boolean previouslyShared = false;
        for (SharedTripRecord sharedTrip : sharedTrips) {
            if (sharedTrip.getTrip() == trip) {
                previouslyShared = true;
                if (allFriends) sharedTrip.shareWithAllFriends();
                if (sharedFriends != null) sharedTrip.shareWithMoreFriends(sharedFriends);
                break;
            }
        }
        if (!previouslyShared) {
            SharedTripRecord newSharedTrip = new SharedTripRecord(trip, allFriends, sharedFriends);
        }
    }

    public TripLogger getTripLogger() {
        return tripLogger;
    }

    public double getHoursAtSeaThisYear() {
        return yearlyCounter.getColumn(FisherYearlyTimeSeries.HOURS_OUT);
    }

    @Override
    public void start(FishState model, Fisher fisher) {
        yearlyCounter.addColumn(FisherYearlyTimeSeries.FUEL_CONSUMPTION);
        yearlyCounter.addColumn(FisherYearlyTimeSeries.FUEL_EXPENDITURE);
        yearlyCounter.addColumn(FisherYearlyTimeSeries.VARIABLE_COSTS);
        yearlyCounter.addColumn(FisherYearlyTimeSeries.EARNINGS);
        yearlyCounter.addColumn(FisherYearlyTimeSeries.TRIPS);
        yearlyCounter.addColumn(FisherYearlyTimeSeries.EFFORT);
        yearlyCounter.addColumn(FisherYearlyTimeSeries.HOURS_OUT);
        dailyCounter = new FisherDailyCounter(model.getSpecies().size());

        dailyTimeSeries.start(model, fisher);
        yearlyTimeSeries.start(model, fisher);
        yearlyCounter.start(model);
        dailyCounter.start(model);
        tripLogger.start(model);
        tripMemories.start(model);
        tripLogger.addTripListener(new TripListener() {
            @Override
            public void reactToFinishedTrip(TripRecord record, Fisher fisher) {
                SeaTile mostFishedTileInTrip = record.getMostFishedTileInTrip();
                if (mostFishedTileInTrip != null)
                    tripMemories.memorize(record, mostFishedTileInTrip);
            }
        });
    }

    @Override
    public void turnOff(Fisher fisher) {
        tripMemories.turnOff();
        tripLogger.turnOff();
        dailyCounter.turnOff();
        yearlyCounter.turnOff();

        yearlyTimeSeries.turnOff();
        dailyTimeSeries.turnOff();

    }

    public TripRecord getLastFinishedTrip() {
        return tripLogger.getLastFinishedTrip();

    }


    public double balanceXDaysAgo(int daysAgo) {
        //    Preconditions.checkArgument(dailyTimeSeries.numberOfObservations() >daysAgo);
        return dailyTimeSeries.getColumn(FisherYearlyTimeSeries.CASH_COLUMN).getDatumXStepsAgo(daysAgo);
    }


    public int numberOfDailyObservations() {
        return dailyTimeSeries.numberOfObservations();
    }

    /**
     * @param location
     * @return
     */
    public TripRecord rememberLastTripHere(SeaTile location) {

        return getTripMemories().getMemory(location);
    }

    public LocationMemories<TripRecord> getTripMemories() {
        return tripMemories;
    }

    public Map<SeaTile, LocationMemory<TripRecord>> rememberAllTrips() {
        return getTripMemories().getMemories();
    }

    /**
     * Ask the fisher what is the best tile with respect to trips made
     *
     * @param comparator how should the fisher compare each tile remembered
     */
    public SeaTile getBestSpotForTripsRemembered(
        Comparator<LocationMemory<TripRecord>> comparator
    ) {
        return getTripMemories().getBestFishingSpotInMemory(comparator);
    }

    public void addFeatureExtractor(
        String nameOfFeature,
        FeatureExtractor<SeaTile> extractor
    ) {
        tileRepresentation.addFeatureExtractor(nameOfFeature, extractor);
    }

    public FeatureExtractor<SeaTile> removeFeatureExtractor(String nameOfFeature) {
        return tileRepresentation.removeFeatureExtractor(nameOfFeature);
    }

    /**
     * Getter for property 'tileRepresentation'.
     *
     * @return Value for property 'tileRepresentation'.
     */
    public FeatureExtractors<SeaTile> getTileRepresentation() {
        return tileRepresentation;
    }


    /**
     * keep that memory in the database. The key cannot be currently in use!
     *
     * @param key  the key for the object
     * @param item the object to store
     */
    public void memorize(String key, Object item) {
        Object previous = database.put(key, item);
        Preconditions.checkState(previous == null, "The database already contains this key");
    }

    /**
     * removes the memory associated with that key
     *
     * @param key
     */
    public void forget(String key) {
        database.remove(key);
    }

    /**
     * returns the object associated with this key
     *
     * @param key
     */
    public Object remember(String key) {
        return database.get(key);
    }


    /**
     * registers visit (if the memory exists)
     */
    public void registerVisit(int group, int day) {
        if (discretizedLocationMemory != null)
            discretizedLocationMemory.registerVisit(group, day);
    }

    /**
     * registers visit (if the memory exists)
     */
    public void registerVisit(SeaTile tile, int day) {
        if (discretizedLocationMemory != null)
            discretizedLocationMemory.registerVisit(tile, day);
    }

    public DiscretizedLocationMemory getDiscretizedLocationMemory() {
        return discretizedLocationMemory;
    }

    public void setDiscretizedLocationMemory(DiscretizedLocationMemory discretizedLocationMemory) {
        Preconditions.checkArgument(
            this.discretizedLocationMemory == null,
            "Rewriting a discretized location memory with another. Probably not what you want!"
        );
        this.discretizedLocationMemory = discretizedLocationMemory;
    }
}