package uk.ac.ox.oxfish.model;

/**
 * Anything that needs to be started
 * Created by carrknight on 5/5/15.
 */
public interface Startable {


    /**
     * this gets called by the fish-state right after the scenario has started. It's useful to set up steppables
     * or just to percolate a reference to the model
     * @param model the model
     */
    public void start(FishState model);

    /**
     * tell the startable to turnoff,
     */
    public void turnOff();
}
