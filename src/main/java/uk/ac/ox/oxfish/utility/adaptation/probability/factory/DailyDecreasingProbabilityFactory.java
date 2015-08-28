package uk.ac.ox.oxfish.utility.adaptation.probability.factory;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.adaptation.probability.DailyDecreasingProbability;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * Creates an adaptation probability that decreases over time
 * Created by carrknight on 8/28/15.
 */
public class DailyDecreasingProbabilityFactory implements AlgorithmFactory<DailyDecreasingProbability> {

    private DoubleParameter explorationProbability = new FixedDoubleParameter(.8);

    private DoubleParameter imitationProbability = new FixedDoubleParameter(1);

    private DoubleParameter dailyDecreaseMultiplier = new FixedDoubleParameter(.99);

    private DoubleParameter explorationProbabilityMinimum = new FixedDoubleParameter(.01);


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public DailyDecreasingProbability apply(FishState state) {
        return new DailyDecreasingProbability(explorationProbability.apply(state.getRandom()),
                                              imitationProbability.apply(state.getRandom()),
                                              dailyDecreaseMultiplier.apply(state.getRandom()),
                                              explorationProbabilityMinimum.apply(state.getRandom()));
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

    public DoubleParameter getDailyDecreaseMultiplier() {
        return dailyDecreaseMultiplier;
    }

    public void setDailyDecreaseMultiplier(DoubleParameter dailyDecreaseMultiplier) {
        this.dailyDecreaseMultiplier = dailyDecreaseMultiplier;
    }

    public DoubleParameter getExplorationProbabilityMinimum() {
        return explorationProbabilityMinimum;
    }

    public void setExplorationProbabilityMinimum(
            DoubleParameter explorationProbabilityMinimum) {
        this.explorationProbabilityMinimum = explorationProbabilityMinimum;
    }
}
