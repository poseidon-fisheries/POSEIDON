package uk.ac.ox.oxfish.fisher.log;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
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
public class TripLogger implements Startable
{

    /**
     * this is really null only until the first trip starts.
     */
    private TripRecord currentTrip = null;

    /**
     * trips trips trips
     */
    private final LinkedList<TripRecord> finishedTrips = new LinkedList<>();

    private TripRecord lastFinishedTrip = null;

    /**
     * whatever needs to be notified that a trip is complete
     */
    private final List<TripListener> listeners = new LinkedList<>();


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

    public void addTripListener(TripListener listener)
    {
        Preconditions.checkArgument(!listeners.contains(listener));
        listeners.add(listener);
    }

    public void removeTripListener(TripListener listener)
    {
        listeners.remove(listener);
    }

    /**
     * log the start of a new trip
     * @param hoursAtPort
     */
    public void newTrip(double hoursAtPort){
        assert currentTrip == null || currentTrip.isCompleted(); //the other trip is over
        //just replace the old trip
        currentTrip = new TripRecord(numberOfSpecies, hoursAtPort);
    }

    /**
     * log the trip is over
     */
    public void finishTrip(double hoursAtSea, Port terminal)
    {

        //it must have taken more than 0 hours to do a trip
        Preconditions.checkArgument(hoursAtSea > 0);

        //complete the trip
        currentTrip.completeTrip(hoursAtSea, terminal);
        //add it to the historical record
        finishedTrips.add(currentTrip);
        lastFinishedTrip = currentTrip;
        //tell the listeners
        for(TripListener listener : listeners)
            listener.reactToFinishedTrip(currentTrip);

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
     *
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
