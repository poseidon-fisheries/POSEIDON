package uk.ac.ox.oxfish.biology.complicated.factory;

import uk.ac.ox.oxfish.biology.complicated.NoAbundanceDiffusion;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

/**
 * Created by carrknight on 7/8/17.
 */
public class NoDiffuserFactory  implements AlgorithmFactory<NoAbundanceDiffusion>{


    /**
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public NoAbundanceDiffusion apply(FishState fishState) {
        return new NoAbundanceDiffusion();
    }
}
