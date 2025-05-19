/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.fisher.log;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Holds a list of all previous trips + can notify listeners that a trip has ended .
 * Created by carrknight on 6/17/15.
 */
public class TripLogger implements Startable {

    /**
     * trips trips trips
     */
    private final LinkedList<TripRecord> finishedTrips = new LinkedList<>();
    /**
     * whatever needs to be notified that a trip is complete
     */
    private final List<TripListener> listeners = new LinkedList<>();
    /**
     * this is really null only until the first trip starts.
     */
    private TripRecord currentTrip = null;
    private TripRecord lastFinishedTrip = null;
    private int numberOfSpecies = -1;

    /**
     * ignored
     */
    @Override
    public void start(FishState model) {
        numberOfSpecies = model.getSpecies().size();
    }

    /**
     * clear all listeners
     */
    @Override
    public void turnOff() {
        listeners.clear();
    }

    public void addTripListener(TripListener listener) {
        Preconditions.checkArgument(!listeners.contains(listener));
        listeners.add(listener);
    }

    public void removeTripListener(TripListener listener) {
        listeners.remove(listener);
    }

    /**
     * log the start of a new trip
     *
     * @param hoursAtPort
     */
    public void newTrip(double hoursAtPort, int tripDay, Fisher fisher) {
        assert currentTrip == null || currentTrip.isCompleted(); //the other trip is over
        //just replace the old trip
        currentTrip = new TripRecord(numberOfSpecies, hoursAtPort, tripDay);
        //tell the listeners
        for (TripListener listener : listeners)
            listener.reactToNewTrip(currentTrip, fisher);
    }

    /**
     * log the trip is over
     */
    public TripRecord finishTrip(double hoursAtSea, Port terminal, Fisher fisher) {

        //it must have taken more than 0 hours to do a trip
        Preconditions.checkArgument(hoursAtSea > 0);

        //complete the trip
        currentTrip.completeTrip(hoursAtSea, terminal);
        //add it to the historical record
        finishedTrips.add(currentTrip);
        lastFinishedTrip = currentTrip;
        //tell the listeners
        for (TripListener listener : listeners)
            listener.reactToFinishedTrip(currentTrip, fisher);

        return currentTrip;
    }


    public void resetTrip() {
        currentTrip = null;
    }

    public TripRecord getCurrentTrip() {
        return currentTrip;
    }

    public void recordCosts(double newCosts) {
        currentTrip.recordCosts(newCosts);
    }

    public void recordFishing(FishingRecord record) {
        currentTrip.recordFishing(record);
    }

    public void recordTripCutShort() {
        currentTrip.recordTripCutShort();
    }

    public List<TripRecord> getFinishedTrips() {
        return Collections.unmodifiableList(finishedTrips);
    }

    public TripRecord getLastFinishedTrip() {
        return lastFinishedTrip;
    }


    /**
     * @param specieIndex
     * @param biomass
     * @param earnings
     */
    public void recordEarnings(int specieIndex, double biomass, double earnings) {
        currentTrip.recordEarnings(specieIndex, biomass, earnings);
    }

    @VisibleForTesting
    public void setNumberOfSpecies(int numberOfSpecies) {
        this.numberOfSpecies = numberOfSpecies;
    }

    public void recordOpportunityCosts(double implicitCost) {
        currentTrip.recordOpportunityCosts(implicitCost);
    }
}
