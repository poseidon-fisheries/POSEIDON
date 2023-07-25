package uk.ac.ox.oxfish.fisher.purseseiner.samplers;

import com.google.common.collect.ForwardingMap;
import com.google.common.collect.ImmutableMap;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.AbstractSetAction;

import java.util.Map;

public class CatchSamplers<B extends LocalBiology>
    extends ForwardingMap<Class<? extends AbstractSetAction>, CatchSampler<B>> {

    private final Map<Class<? extends AbstractSetAction>, CatchSampler<B>> delegate;

    public CatchSamplers(final Map<Class<? extends AbstractSetAction>, CatchSampler<B>> delegate) {
        this.delegate = ImmutableMap.copyOf(delegate);
    }

    @Override
    protected Map<Class<? extends AbstractSetAction>, CatchSampler<B>> delegate() {
        return delegate;
    }
}
