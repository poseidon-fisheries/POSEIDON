package uk.ac.ox.oxfish.model.plugins;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.log.initializers.TowAndAltitudeOutputInitializer;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;

import javax.annotation.Nullable;

/**
 * just like the TowAndAltitudeOutputInitializer except that unlike logbook interface that waits for you to feed it
 * agents, this one tracks all agents when start is called!
 */
public class TowAndAltitudePlugin implements AdditionalStartable {


    private final TowAndAltitudeOutputInitializer delegate;

    private final String tag;


    public TowAndAltitudePlugin(
        final int histogrammerStartYear, final String identifier,
        @Nullable final
        String tag
    ) {

        this.tag = tag;
        this.delegate = new TowAndAltitudeOutputInitializer(histogrammerStartYear, identifier);
    }


    @Override
    public void start(final FishState model) {
        delegate.start(model);
        for (final Fisher fisher : model.getFishers()) {
            if (tag == null || fisher.getTagsList().contains(tag)) {

                delegate.add(fisher, model);

            }
        }
    }

    @Override
    public void turnOff() {

        delegate.turnOff();
    }
}
