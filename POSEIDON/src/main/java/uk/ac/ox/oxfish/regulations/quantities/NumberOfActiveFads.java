package uk.ac.ox.oxfish.regulations.quantities;

import uk.ac.ox.poseidon.common.api.ComponentFactory;
import uk.ac.ox.poseidon.common.api.ModelState;
import uk.ac.ox.poseidon.regulations.api.Quantity;

import static com.google.common.base.Preconditions.checkArgument;

public class NumberOfActiveFads implements ComponentFactory<Quantity> {

    private static final Quantity NUMBER_OF_ACTIVE_FADS = action -> {
        checkArgument(action instanceof Getter);
        return ((Getter) action).getNumberOfActiveFads();
    };

    public NumberOfActiveFads() {
    }

    @Override
    public Quantity apply(final ModelState ignored) {
        return NUMBER_OF_ACTIVE_FADS;
    }

    public interface Getter {
        long getNumberOfActiveFads();
    }
}
