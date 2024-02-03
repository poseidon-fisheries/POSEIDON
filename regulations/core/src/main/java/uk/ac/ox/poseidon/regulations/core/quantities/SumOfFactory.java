package uk.ac.ox.poseidon.regulations.core.quantities;

import com.google.common.collect.ImmutableList;
import uk.ac.ox.poseidon.common.api.ComponentFactory;
import uk.ac.ox.poseidon.common.api.ModelState;
import uk.ac.ox.poseidon.regulations.api.Quantity;

import java.util.Collection;

import static com.google.common.collect.ImmutableList.toImmutableList;

public class SumOfFactory implements ComponentFactory<Quantity> {

    private Collection<ComponentFactory<Quantity>> quantities;

    @SuppressWarnings("unused")
    public SumOfFactory() {
    }

    @SafeVarargs
    @SuppressWarnings("varargs")
    public SumOfFactory(final ComponentFactory<Quantity>... quantities) {
        this(ImmutableList.copyOf(quantities));
    }

    @SuppressWarnings("WeakerAccess")
    public SumOfFactory(final Collection<ComponentFactory<Quantity>> quantities) {
        this.quantities = quantities;
    }

    public Collection<ComponentFactory<Quantity>> getQuantities() {
        return quantities;
    }

    public void setQuantities(final Collection<ComponentFactory<Quantity>> quantities) {
        this.quantities = quantities;
    }

    @Override
    public Quantity apply(final ModelState modelState) {
        return new SumOf(
            quantities.stream().map(q -> q.apply(modelState)).collect(toImmutableList())
        );
    }
}
