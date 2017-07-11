package uk.ac.ox.oxfish.biology.initializer.allocator;

import uk.ac.ox.oxfish.biology.initializer.allocator.ConstantBiomassAllocator;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

/**
 * Created by carrknight on 7/11/17.
 */
public class ConstantAllocatorFactory implements AlgorithmFactory<ConstantBiomassAllocator> {


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public ConstantBiomassAllocator apply(FishState state) {
        return new ConstantBiomassAllocator();
    }
}
