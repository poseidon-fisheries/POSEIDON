package uk.ac.ox.oxfish.model.plugins;

import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.log.initializers.TowAndAltitudeOutputInitializer;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;

import javax.annotation.Nullable;

/**
 * just like the TowAndAltitudeOutputInitializer except that unlike logbook interface that waits for you to feed it
 * agents, this one tracks all agents when start is called!
 */
public class TowAndAltitudePlugin implements AdditionalStartable {


    private final TowAndAltitudeOutputInitializer delegate;

    private final String tag;


    public TowAndAltitudePlugin(int histogrammerStartYear, String identifier,
                                @Nullable
                                String tag) {

        this.tag = tag;
        this.delegate = new TowAndAltitudeOutputInitializer(histogrammerStartYear,identifier);
    }


    @Override
    public void start(FishState model) {
        delegate.start(model);
        for (Fisher fisher : model.getFishers()) {
            if(tag == null || fisher.getTags().contains(tag)) {

                delegate.add(fisher,model);

            }
        }
    }

    @Override
    public void turnOff() {

        delegate.turnOff();
    }
}
