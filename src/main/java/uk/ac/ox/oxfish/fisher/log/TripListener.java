package uk.ac.ox.oxfish.fisher.log;

/**
 * Any object that needs to be notified a trip is over
 * Created by carrknight on 6/17/15.
 */
public interface TripListener {

    void reactToFinishedTrip(TripRecord record);

}
