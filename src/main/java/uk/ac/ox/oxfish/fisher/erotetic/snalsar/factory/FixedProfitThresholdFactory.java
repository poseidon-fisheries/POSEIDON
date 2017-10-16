package uk.ac.ox.oxfish.fisher.erotetic.snalsar.factory;

import uk.ac.ox.oxfish.fisher.erotetic.snalsar.FixedProfitThresholdExtractor;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * Creates a Fixed Profit Threshold Extractor
 * Created by carrknight on 6/8/16.
 */
public class FixedProfitThresholdFactory implements AlgorithmFactory<FixedProfitThresholdExtractor>
{

    /**
     * the threshold (fixed for all elements)
     */
    private DoubleParameter  fixedThreshold = new FixedDoubleParameter(0d);


    public FixedProfitThresholdFactory() {
    }

    public FixedProfitThresholdFactory(double threshold) {
        fixedThreshold = new FixedDoubleParameter(threshold);
    }

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public FixedProfitThresholdExtractor apply(FishState state) {
        return new FixedProfitThresholdExtractor(fixedThreshold.apply(state.getRandom()));
    }


    /**
     * Getter for property 'fixedThreshold'.
     *
     * @return Value for property 'fixedThreshold'.
     */
    public DoubleParameter getFixedThreshold() {
        return fixedThreshold;
    }

    /**
     * Setter for property 'fixedThreshold'.
     *
     * @param fixedThreshold Value to set for property 'fixedThreshold'.
     */
    public void setFixedThreshold(DoubleParameter fixedThreshold) {
        this.fixedThreshold = fixedThreshold;
    }
}
