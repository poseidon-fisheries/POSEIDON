package uk.ac.ox.oxfish.regulations.conditions;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.poseidon.regulations.api.Condition;
import uk.ac.ox.poseidon.regulations.api.Quantity;

public class Above implements AlgorithmFactory<Condition> {

    private AlgorithmFactory<? extends Quantity> quantity;
    private DoubleParameter limit;

    public Above() {
    }

    public Above(
        final AlgorithmFactory<? extends Quantity> quantity,
        final double limit
    ) {
        this(quantity, new FixedDoubleParameter(limit));
    }

    @SuppressWarnings("WeakerAccess")
    public Above(
        final AlgorithmFactory<? extends Quantity> quantity, final DoubleParameter limit
    ) {
        this.limit = limit;
        this.quantity = quantity;
    }

    public AlgorithmFactory<? extends Quantity> getQuantity() {
        return quantity;
    }

    public void setQuantity(final AlgorithmFactory<? extends Quantity> quantity) {
        this.quantity = quantity;
    }

    public DoubleParameter getLimit() {
        return limit;
    }

    public void setLimit(final DoubleParameter limit) {
        this.limit = limit;
    }

    @Override
    public Condition apply(final FishState fishState) {
        return new uk.ac.ox.poseidon.regulations.core.conditions.Above(
            limit.applyAsDouble(fishState.getRandom()),
            quantity.apply(fishState)
        );
    }
}