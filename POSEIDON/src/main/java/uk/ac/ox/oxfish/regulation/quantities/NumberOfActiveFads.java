package uk.ac.ox.oxfish.regulation.quantities;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.regulations.api.Quantity;

import static com.google.common.base.Preconditions.checkArgument;

public class NumberOfActiveFads implements AlgorithmFactory<Quantity> {

    private static final Quantity NUMBER_OF_ACTIVE_FADS = action -> {
        checkArgument(action instanceof Getter);
        return ((Getter) action).getNumberOfActiveFads();
    };

    public NumberOfActiveFads() {
    }

    @Override
    public Quantity apply(final FishState fishState) {
        return NUMBER_OF_ACTIVE_FADS;
    }

    public interface Getter {
        long getNumberOfActiveFads();
    }
}
