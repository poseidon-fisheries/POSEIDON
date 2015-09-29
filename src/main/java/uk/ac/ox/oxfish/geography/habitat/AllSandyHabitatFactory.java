package uk.ac.ox.oxfish.geography.habitat;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

/**
 * Creates the initializer
 * Created by carrknight on 9/29/15.
 */
public class AllSandyHabitatFactory implements AlgorithmFactory<AllSandyHabitatInitializer>
{


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public AllSandyHabitatInitializer apply(FishState state) {
        return new AllSandyHabitatInitializer();
    }
}
