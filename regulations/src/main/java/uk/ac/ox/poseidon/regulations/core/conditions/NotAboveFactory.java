package uk.ac.ox.poseidon.regulations.core.conditions;

import uk.ac.ox.poseidon.common.api.ComponentFactory;
import uk.ac.ox.poseidon.common.api.ModelState;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;
import uk.ac.ox.poseidon.regulations.api.Condition;
import uk.ac.ox.poseidon.regulations.api.Quantity;

public class NotAboveFactory implements ComponentFactory<Condition> {

    private ComponentFactory<? extends Quantity> quantity;
    private DoubleParameter limit;

    public NotAboveFactory() {
    }

    public NotAboveFactory(
        final ComponentFactory<? extends Quantity> quantity,
        final double limit
    ) {
        this(quantity, new FixedDoubleParameter(limit));
    }

    @SuppressWarnings("WeakerAccess")
    public NotAboveFactory(
        final ComponentFactory<? extends Quantity> quantity,
        final DoubleParameter limit
    ) {
        this.limit = limit;
        this.quantity = quantity;
    }

    public ComponentFactory<? extends Quantity> getQuantity() {
        return quantity;
    }

    public void setQuantity(final ComponentFactory<? extends Quantity> quantity) {
        this.quantity = quantity;
    }

    public DoubleParameter getLimit() {
        return limit;
    }

    public void setLimit(final DoubleParameter limit) {
        this.limit = limit;
    }

    @Override
    public Condition apply(final ModelState modelState) {
        return new uk.ac.ox.poseidon.regulations.core.conditions.Not(
            new uk.ac.ox.poseidon.regulations.core.conditions.Above(
                limit.applyAsDouble(modelState.getRandom()),
                quantity.apply(modelState)
            )
        );
    }
}
