package uk.ac.ox.oxfish.fisher.purseseiner.samplers;

import com.google.common.collect.ForwardingMap;
import com.google.common.collect.ImmutableMap;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.AbstractSetAction;

import java.util.Map;
import java.util.stream.Collectors;

public class CatchSamplers<B extends LocalBiology>
    extends ForwardingMap<Object, CatchSampler<B>> {

    private final Map<Object, CatchSampler<B>> delegate;

    public CatchSamplers(final Map<Object, CatchSampler<B>> delegate) {
        this.delegate = ImmutableMap.copyOf(delegate);
    }

    @Override
    protected Map<Object, CatchSampler<B>> delegate() {
        return delegate;
    }
}
