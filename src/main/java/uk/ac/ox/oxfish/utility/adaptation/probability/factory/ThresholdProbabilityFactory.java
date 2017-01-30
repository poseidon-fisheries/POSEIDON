package uk.ac.ox.oxfish.utility.adaptation.probability.factory;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.adaptation.probability.ThresholdExplorationProbability;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.function.Function;

/**
 * Created by carrknight on 1/29/17.
 */
public class ThresholdProbabilityFactory implements AlgorithmFactory<ThresholdExplorationProbability> {


    private DoubleParameter threshold = new FixedDoubleParameter(1);


    /**
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public ThresholdExplorationProbability apply(FishState fishState) {
        final double actualThreshold = threshold.apply(fishState.getRandom());

        return new ThresholdExplorationProbability(1d,
                                                   new Function<FishState, Double>() {
                                                       @Override
                                                       public Double apply(FishState fishState) {
                                                           return actualThreshold;
                                                       }
                                                   });
    }

    /**
     * Getter for property 'threshold'.
     *
     * @return Value for property 'threshold'.
     */
    public DoubleParameter getThreshold() {
        return threshold;
    }

    /**
     * Setter for property 'threshold'.
     *
     * @param threshold Value to set for property 'threshold'.
     */
    public void setThreshold(DoubleParameter threshold) {
        this.threshold = threshold;
    }
}
