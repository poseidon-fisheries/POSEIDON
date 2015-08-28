package uk.ac.ox.oxfish.utility.adaptation.probability.factory;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.adaptation.probability.FixedProbability;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * Creates an adaption probability that is fixed
 * Created by carrknight on 8/28/15.
 */
public class FixedProbabilityFactory implements AlgorithmFactory<FixedProbability> {

    private DoubleParameter explorationProbability = new FixedDoubleParameter(.8);

    private DoubleParameter imitationProbability = new FixedDoubleParameter(1);


    public FixedProbabilityFactory() {
    }

    public FixedProbabilityFactory(
            double explorationProbability,
            double imitationProbability) {
        this.explorationProbability = new FixedDoubleParameter(explorationProbability);
        this.imitationProbability = new FixedDoubleParameter(imitationProbability);
    }


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public FixedProbability apply(FishState state) {
        return new FixedProbability(explorationProbability.apply(state.getRandom()),
                                    imitationProbability.apply(state.getRandom()));
    }

    public DoubleParameter getExplorationProbability() {
        return explorationProbability;
    }

    public void setExplorationProbability(DoubleParameter explorationProbability) {
        this.explorationProbability = explorationProbability;
    }

    public DoubleParameter getImitationProbability() {
        return imitationProbability;
    }

    public void setImitationProbability(DoubleParameter imitationProbability) {
        this.imitationProbability = imitationProbability;
    }
}
