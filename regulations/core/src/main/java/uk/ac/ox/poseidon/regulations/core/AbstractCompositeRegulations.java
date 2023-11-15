package uk.ac.ox.poseidon.regulations.core;

import com.google.common.collect.ImmutableList;
import uk.ac.ox.poseidon.regulations.api.Regulations;

import java.util.Collection;

public abstract class AbstractCompositeRegulations implements Regulations {
    private final Collection<Regulations> subRegulations;

    protected AbstractCompositeRegulations(
        final Collection<Regulations> subRegulations
    ) {
        this.subRegulations = ImmutableList.copyOf(subRegulations);
    }

    public Collection<Regulations> getSubRegulations() {
        return subRegulations;
    }

}
