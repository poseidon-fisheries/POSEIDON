package uk.ac.ox.oxfish.regulation;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.regulations.api.Regulation;

import java.util.Map;

import static com.google.common.collect.ImmutableSet.toImmutableSet;

public class NamedRegulations implements AlgorithmFactory<Regulation> {
    private Map<String, AlgorithmFactory<Regulation>> regulations;

    public NamedRegulations(final Map<String, AlgorithmFactory<Regulation>> regulations) {
        this.regulations = regulations;
    }

    public NamedRegulations() {
    }

    public Map<String, AlgorithmFactory<Regulation>> getRegulations() {
        return regulations;
    }

    public void setRegulations(final Map<String, AlgorithmFactory<Regulation>> regulations) {
        this.regulations = regulations;
    }

    @Override
    public Regulation apply(final FishState fishState) {
        return new uk.ac.ox.poseidon.regulations.core.ConjunctiveRegulation(
            regulations.values().stream()
                .map(regulation -> regulation.apply(fishState))
                .collect(toImmutableSet())
        );
    }
}
