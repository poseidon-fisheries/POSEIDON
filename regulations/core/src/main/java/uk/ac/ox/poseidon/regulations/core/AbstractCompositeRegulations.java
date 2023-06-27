package uk.ac.ox.poseidon.regulations.core;

import com.google.common.collect.ImmutableList;
import uk.ac.ox.poseidon.regulations.api.CompositeRegulations;
import uk.ac.ox.poseidon.regulations.api.Regulations;

import java.util.Collection;
import java.util.stream.Stream;

public abstract class AbstractCompositeRegulations<C> implements CompositeRegulations<C> {
    private final Collection<Regulations<C>> subRegulations;

    protected AbstractCompositeRegulations(
        final Collection<Regulations<C>> subRegulations
    ) {
        this.subRegulations = ImmutableList.copyOf(subRegulations);
    }

    @Override
    public Collection<Regulations<C>> getSubRegulations() {
        return subRegulations;
    }

    @Override
    public Stream<Regulations<C>> asStream() {
        return subRegulations.stream().flatMap(
            Regulations::asStream
        );
    }
}
