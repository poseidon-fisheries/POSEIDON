package uk.ac.ox.oxfish.fisher.log;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;

import java.util.*;

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
    private final LinkedList<TripRecord> allTrips = new LinkedList<>();

    /**
     * whatever needs to be notified that a trip is complete
     */
    private final Set<TripListener> listeners = new HashSet<>();


    /**
     * ignored
     */
    @Override
    public void start(FishState model) {

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
        listeners.add(listener);
    }

    public void removeTripListener(TripListener listener)
    {
        listeners.remove(listener);
    }

    /**
     * log the start of a new trip
     */
    public void newTrip(){
        assert currentTrip == null || currentTrip.isCompleted(); //the other trip is over
        //just replace the old trip
        currentTrip = new TripRecord();
    }

    /**
     * log the trip is over
     */
    public void finishTrip(int stepsAtSea)
    {
        //complete the trip
        currentTrip.completeTrip(stepsAtSea);
        //add it to the historical record
        allTrips.add(currentTrip);
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

    public void recordEarnings(double newEarnings) {
        currentTrip.recordEarnings(newEarnings);
    }

    public void recordTripCutShort() {
        currentTrip.recordTripCutShort();
    }

    public List<TripRecord> getAllTrips() {
        return Collections.unmodifiableList(allTrips);
    }
}
