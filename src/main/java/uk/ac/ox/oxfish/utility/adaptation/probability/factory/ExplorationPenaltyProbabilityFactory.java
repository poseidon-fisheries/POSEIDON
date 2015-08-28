package uk.ac.ox.oxfish.utility.adaptation.probability.factory;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.adaptation.probability.ExplorationPenaltyProbability;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * Creates an adaptation probability that increases as exploration works and decreases otherwise
 * Created by carrknight on 8/28/15.
 */
public class ExplorationPenaltyProbabilityFactory
        implements AlgorithmFactory<ExplorationPenaltyProbability> {

    private DoubleParameter explorationProbability = new FixedDoubleParameter(.8);

    private DoubleParameter imitationProbability = new FixedDoubleParameter(1);

    private DoubleParameter incrementMultiplier = new FixedDoubleParameter(.02);

    private DoubleParameter explorationProbabilityMinimum = new FixedDoubleParameter(.01);


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public ExplorationPenaltyProbability apply(FishState state) {
        return new ExplorationPenaltyProbability(explorationProbability.apply(state.getRandom()),
                                                 imitationProbability.apply(state.getRandom()),
                                                 incrementMultiplier.apply(state.getRandom()),
                                                 explorationProbabilityMinimum.apply(state.getRandom()));
    }


    public ExplorationPenaltyProbabilityFactory() {
    }

    public ExplorationPenaltyProbabilityFactory(
            double explorationProbability, double imitationProbability,
            double incrementMultiplier,
            double explorationProbabilityMinimum) {
        this.explorationProbability = new FixedDoubleParameter(explorationProbability);
        this.imitationProbability = new FixedDoubleParameter(imitationProbability);
        this.incrementMultiplier = new FixedDoubleParameter(incrementMultiplier);
        this.explorationProbabilityMinimum = new FixedDoubleParameter(explorationProbabilityMinimum);
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

    public DoubleParameter getIncrementMultiplier() {
        return incrementMultiplier;
    }

    public void setIncrementMultiplier(DoubleParameter incrementMultiplier) {
        this.incrementMultiplier = incrementMultiplier;
    }

    public DoubleParameter getExplorationProbabilityMinimum() {
        return explorationProbabilityMinimum;
    }

    public void setExplorationProbabilityMinimum(
            DoubleParameter explorationProbabilityMinimum) {
        this.explorationProbabilityMinimum = explorationProbabilityMinimum;
    }
}
