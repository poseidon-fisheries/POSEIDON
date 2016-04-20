package uk.ac.ox.oxfish.fisher.log;

import java.io.Serializable;

/**
 * Any object that needs to be notified a trip is over
 * Created by carrknight on 6/17/15.
 */
public interface TripListener extends Serializable{

    void reactToFinishedTrip(TripRecord record);

}
