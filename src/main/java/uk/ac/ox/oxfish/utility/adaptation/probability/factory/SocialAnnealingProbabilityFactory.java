package uk.ac.ox.oxfish.utility.adaptation.probability.factory;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.adaptation.probability.SocialAnnealingProbability;

/**
 * Created by carrknight on 10/17/16.
 */
public class SocialAnnealingProbabilityFactory implements AlgorithmFactory<SocialAnnealingProbability>{

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public SocialAnnealingProbability apply(FishState state) {
        return new SocialAnnealingProbability();
    }
}
