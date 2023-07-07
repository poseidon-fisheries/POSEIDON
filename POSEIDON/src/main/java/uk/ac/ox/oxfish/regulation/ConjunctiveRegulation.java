package uk.ac.ox.oxfish.regulation;

import com.google.common.collect.ImmutableList;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.regulations.api.Regulation;

import java.util.Collection;

import static com.google.common.collect.ImmutableSet.toImmutableSet;

public class ConjunctiveRegulation implements AlgorithmFactory<Regulation> {
    private Collection<AlgorithmFactory<Regulation>> regulations;

    @SafeVarargs
    @SuppressWarnings("varargs")
    public ConjunctiveRegulation(final AlgorithmFactory<Regulation>... regulations) {
        this(ImmutableList.copyOf(regulations));
    }

    public ConjunctiveRegulation(final Collection<? extends AlgorithmFactory<Regulation>> regulations) {
        this.regulations = ImmutableList.copyOf(regulations);
    }

    public ConjunctiveRegulation() {
    }

    public Collection<AlgorithmFactory<Regulation>> getRegulations() {
        return regulations;
    }

    public void setRegulations(final Collection<AlgorithmFactory<Regulation>> regulations) {
        this.regulations = regulations;
    }

    @Override
    public Regulation apply(final FishState fishState) {
        return new uk.ac.ox.poseidon.regulations.core.ConjunctiveRegulation(
            regulations.stream()
                .map(regulation -> regulation.apply(fishState))
                .collect(toImmutableSet())
        );
    }
}
