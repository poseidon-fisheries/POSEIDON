package uk.ac.ox.oxfish.regulation.quantities;

import com.google.common.collect.ImmutableList;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.regulations.api.Quantity;

import java.util.Collection;

import static com.google.common.collect.ImmutableList.toImmutableList;

public class SumOf implements AlgorithmFactory<Quantity> {

    private Collection<AlgorithmFactory<Quantity>> quantities;

    @SuppressWarnings("unused")
    public SumOf() {
    }

    @SafeVarargs
    @SuppressWarnings("varargs")
    public SumOf(final AlgorithmFactory<Quantity>... quantities) {
        this(ImmutableList.copyOf(quantities));
    }

    @SuppressWarnings("WeakerAccess")
    public SumOf(final Collection<AlgorithmFactory<Quantity>> quantities) {
        this.quantities = quantities;
    }

    public Collection<AlgorithmFactory<Quantity>> getQuantities() {
        return quantities;
    }

    public void setQuantities(final Collection<AlgorithmFactory<Quantity>> quantities) {
        this.quantities = quantities;
    }

    @Override
    public Quantity apply(final FishState fishState) {
        return new uk.ac.ox.poseidon.regulations.core.quantities.SumOf(
            quantities.stream().map(q -> q.apply(fishState)).collect(toImmutableList())
        );
    }
}
