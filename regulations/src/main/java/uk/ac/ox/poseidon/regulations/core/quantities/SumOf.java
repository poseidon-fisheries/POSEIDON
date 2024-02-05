package uk.ac.ox.poseidon.regulations.core.quantities;

import com.google.common.collect.ImmutableList;
import uk.ac.ox.poseidon.agents.api.Action;
import uk.ac.ox.poseidon.regulations.api.Quantity;

import java.util.Collection;

class SumOf implements Quantity {

    private final Collection<? extends Quantity> quantities;

    SumOf(final Collection<? extends Quantity> quantities) {
        this.quantities = ImmutableList.copyOf(quantities);
    }

    @Override
    public double applyAsDouble(final Action action) {
        return quantities.stream().mapToDouble(q -> q.applyAsDouble(action)).sum();
    }
}
