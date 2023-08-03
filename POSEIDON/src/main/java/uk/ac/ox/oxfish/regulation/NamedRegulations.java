package uk.ac.ox.oxfish.regulation;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.regulations.api.Regulations;
import uk.ac.ox.poseidon.regulations.core.ConjunctiveRegulations;

import java.util.Map;

import static com.google.common.collect.ImmutableSet.toImmutableSet;

public class NamedRegulations implements AlgorithmFactory<Regulations> {
    private Map<String, AlgorithmFactory<Regulations>> regulations;

    public NamedRegulations(final Map<String, AlgorithmFactory<Regulations>> regulations) {
        this.regulations = regulations;
    }

    public NamedRegulations() {
    }

    public Map<String, AlgorithmFactory<Regulations>> getRegulations() {
        return regulations;
    }

    public void setRegulations(final Map<String, AlgorithmFactory<Regulations>> regulations) {
        this.regulations = regulations;
    }

    @Override
    public Regulations apply(final FishState fishState) {
        return new ConjunctiveRegulations(
            regulations.values().stream()
                .map(regulation -> regulation.apply(fishState))
                .collect(toImmutableSet())
        );
    }
}
