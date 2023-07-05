package uk.ac.ox.poseidon.regulations.core;

import com.google.common.collect.ImmutableList;
import uk.ac.ox.poseidon.regulations.api.Regulation;

import java.util.Collection;

public abstract class AbstractCompositeRegulation implements Regulation {
    private final Collection<Regulation> subRegulations;

    protected AbstractCompositeRegulation(
        final Collection<Regulation> subRegulations
    ) {
        this.subRegulations = ImmutableList.copyOf(subRegulations);
    }

    protected Collection<Regulation> getSubRegulations() {
        return subRegulations;
    }

}
