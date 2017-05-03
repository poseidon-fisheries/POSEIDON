package uk.ac.ox.oxfish.fisher.strategies.discarding;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

/**
 * Created by carrknight on 5/3/17.
 */
public class DiscardingAllUnsellableFactory implements AlgorithmFactory<DiscardAllUnsellable> {
    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public DiscardAllUnsellable apply(FishState state) {
        return new DiscardAllUnsellable();
    }
}
