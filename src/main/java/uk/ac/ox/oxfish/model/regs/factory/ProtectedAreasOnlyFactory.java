package uk.ac.ox.oxfish.model.regs.factory;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.ProtectedAreasOnly;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

/**
 * creates a Regulation object whose only rule is not to fish in the MPAs
 * Created by carrknight on 6/14/15.
 */
public class ProtectedAreasOnlyFactory implements AlgorithmFactory<ProtectedAreasOnly>
{


    private static  ProtectedAreasOnly mpa = new ProtectedAreasOnly();

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public ProtectedAreasOnly apply(FishState state) {
        return mpa;
    }
}
