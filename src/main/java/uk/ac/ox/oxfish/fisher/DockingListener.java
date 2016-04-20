package uk.ac.ox.oxfish.fisher;

import java.io.Serializable;

/**
 * Gets notified when fisher has docked. That event occurs after "finishing trip" event is called
 * Created by carrknight on 12/21/15.
 */
public interface DockingListener extends Serializable{

    void dockingEvent(Fisher fisher, Port port);

}
