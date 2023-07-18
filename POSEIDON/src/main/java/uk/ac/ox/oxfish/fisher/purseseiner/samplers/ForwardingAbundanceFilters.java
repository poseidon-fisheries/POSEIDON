package uk.ac.ox.oxfish.fisher.purseseiner.samplers;

import com.google.common.collect.ForwardingMap;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.NonMutatingArrayFilter;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.AbstractSetAction;

import java.util.Map;

public class ForwardingAbundanceFilters
    extends ForwardingMap<Class<? extends AbstractSetAction>, Map<Species, NonMutatingArrayFilter>>
    implements AbundanceFilters {

    private final Map<Class<? extends AbstractSetAction>, Map<Species, NonMutatingArrayFilter>> delegate;

    public ForwardingAbundanceFilters(final Map<Class<? extends AbstractSetAction>, Map<Species, NonMutatingArrayFilter>> delegate) {
        this.delegate = delegate;
    }

    @Override
    protected Map<Class<? extends AbstractSetAction>, Map<Species, NonMutatingArrayFilter>> delegate() {
        return delegate;
    }
}
