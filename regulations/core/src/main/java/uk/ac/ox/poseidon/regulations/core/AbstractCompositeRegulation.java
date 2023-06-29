package uk.ac.ox.poseidon.regulations.core;

import com.google.common.collect.ImmutableList;
import uk.ac.ox.poseidon.regulations.api.CompositeRegulation;
import uk.ac.ox.poseidon.regulations.api.Regulation;

import java.util.Collection;

public abstract class AbstractCompositeRegulation<C> implements CompositeRegulation<C> {
    private final Collection<Regulation<C>> subRegulations;

    protected AbstractCompositeRegulation(
        final Collection<Regulation<C>> subRegulations
    ) {
        this.subRegulations = ImmutableList.copyOf(subRegulations);
    }

    @Override
    public Collection<Regulation<C>> getSubRegulations() {
        return subRegulations;
    }

}
