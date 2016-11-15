package uk.ac.ox.oxfish.model.data.factory;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.ExponentialMovingAverage;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * Builds EMA
 * Created by carrknight on 11/11/16.
 */
public class ExponentialMovingAverageFactory implements AlgorithmFactory<ExponentialMovingAverage<Double>> {


    private DoubleParameter alpha = new FixedDoubleParameter(.2);

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public ExponentialMovingAverage<Double> apply(FishState state) {
        return new ExponentialMovingAverage<>(alpha.apply(state.getRandom()));
    }


    /**
     * Getter for property 'alpha'.
     *
     * @return Value for property 'alpha'.
     */
    public DoubleParameter getAlpha() {
        return alpha;
    }

    /**
     * Setter for property 'alpha'.
     *
     * @param alpha Value to set for property 'alpha'.
     */
    public void setAlpha(DoubleParameter alpha) {
        this.alpha = alpha;
    }
}
