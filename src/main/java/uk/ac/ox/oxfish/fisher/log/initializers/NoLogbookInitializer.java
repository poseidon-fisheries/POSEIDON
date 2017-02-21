package uk.ac.ox.oxfish.fisher.log.initializers;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;

/**
 * Creates no logbook. Default. Saves a lot of energy
 * Created by carrknight on 2/17/17.
 */
public class NoLogbookInitializer implements LogbookInitializer {
    /**
     * this gets called by the fish-state right after the scenario has started. It's useful to set up steppables
     * or just to percolate a reference to the model
     *
     * @param model the model
     */
    @Override
    public void start(FishState model) {

    }

    /**
     * tell the startable to turnoff,
     */
    @Override
    public void turnOff() {

    }

    @Override
    public void add(Fisher fisher, FishState state) {

    }
}
