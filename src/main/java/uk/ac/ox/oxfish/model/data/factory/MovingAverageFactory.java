package uk.ac.ox.oxfish.model.data.factory;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.MovingAverage;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * Builds Moving Average
 * Created by carrknight on 11/11/16.
 */
public class MovingAverageFactory implements AlgorithmFactory<MovingAverage<Double>>{


    private DoubleParameter window = new FixedDoubleParameter(20);

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public MovingAverage<Double> apply(FishState state) {
        return new MovingAverage<>(window.apply(state.getRandom()).intValue());
    }


    /**
     * Getter for property 'window'.
     *
     * @return Value for property 'window'.
     */
    public DoubleParameter getWindow() {
        return window;
    }

    /**
     * Setter for property 'window'.
     *
     * @param window Value to set for property 'window'.
     */
    public void setWindow(DoubleParameter window) {
        this.window = window;
    }
}
