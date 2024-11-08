package uk.ac.ox.poseidon.regulations.core.conditions;

import uk.ac.ox.poseidon.agents.api.Action;
import uk.ac.ox.poseidon.regulations.api.Condition;
import uk.ac.ox.poseidon.regulations.api.Quantity;

public class Below implements Condition {

    private final double limit;
    private final Quantity quantity;

    public Below(
        final double limit,
        final Quantity quantity
    ) {
        this.limit = limit;
        this.quantity = quantity;
    }

    @Override
    public boolean test(final Action action) {
        return quantity.applyAsDouble(action) < limit;
    }

    @Override
    public String toString() {
        return "Below{" +
            "limit=" + limit +
            ", quantity=" + quantity +
            '}';
    }
}
