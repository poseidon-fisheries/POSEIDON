package uk.ac.ox.oxfish.regulations;

import com.google.common.collect.ImmutableMap;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.regulations.api.Regulations;
import uk.ac.ox.poseidon.regulations.core.ConjunctiveRegulations;

import java.util.Map;
import java.util.function.Supplier;

import static com.google.common.collect.ImmutableSet.toImmutableSet;

public class NamedRegulations implements AlgorithmFactory<Regulations> {
    private Map<String, AlgorithmFactory<Regulations>> regulations;

    public NamedRegulations(final Map<String, AlgorithmFactory<Regulations>> regulations) {
        this.regulations = regulations;
    }

    @SuppressWarnings("unused")
    public NamedRegulations() {
    }

    @Override
    public Regulations apply(final FishState fishState) {
        return new ConjunctiveRegulations(
            regulations.values().stream()
                .map(regulation -> regulation.apply(fishState))
                .collect(toImmutableSet())
        );
    }

    public void modify(
        final String regulationName,
        final Supplier<? extends AlgorithmFactory<Regulations>> supplier
    ) {
        setRegulations(
            ImmutableMap.<String, AlgorithmFactory<Regulations>>builder()
                .putAll(getRegulations())
                .put(regulationName, supplier.get())
                .buildKeepingLast()
        );
    }

    public Map<String, AlgorithmFactory<Regulations>> getRegulations() {
        return regulations;
    }

    public void setRegulations(final Map<String, AlgorithmFactory<Regulations>> regulations) {
        this.regulations = regulations;
    }
}
