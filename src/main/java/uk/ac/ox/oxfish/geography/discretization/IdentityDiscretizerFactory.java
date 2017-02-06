package uk.ac.ox.oxfish.geography.discretization;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;


public class IdentityDiscretizerFactory implements AlgorithmFactory<IdentityDiscretizer> {

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public IdentityDiscretizer apply(FishState state) {
        return new IdentityDiscretizer();
    }
}
