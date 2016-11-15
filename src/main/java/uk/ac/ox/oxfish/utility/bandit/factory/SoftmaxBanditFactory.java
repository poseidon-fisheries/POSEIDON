package uk.ac.ox.oxfish.utility.bandit.factory;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.bandit.BanditAlgorithm;
import uk.ac.ox.oxfish.utility.bandit.BanditAverage;
import uk.ac.ox.oxfish.utility.bandit.EpsilonGreedyBanditAlgorithm;
import uk.ac.ox.oxfish.utility.bandit.SoftmaxBanditAlgorithm;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.function.Function;

/**
 * Created by carrknight on 11/11/16.
 */
public class SoftmaxBanditFactory implements
        AlgorithmFactory<BanditSupplier> {


    private DoubleParameter initialTemperature = new FixedDoubleParameter(5d);

    private DoubleParameter temperatureDecay = new FixedDoubleParameter(.98d);


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public BanditSupplier apply(FishState state) {
        return new BanditSupplier() {
            @Override
            public SoftmaxBanditAlgorithm apply(BanditAverage banditAverage) {
                return new SoftmaxBanditAlgorithm(
                        banditAverage,
                        initialTemperature.apply(state.getRandom()),
                        temperatureDecay.apply(state.getRandom())
                );
            }
        };
    }

    /**
     * Getter for property 'initialTemperature'.
     *
     * @return Value for property 'initialTemperature'.
     */
    public DoubleParameter getInitialTemperature() {
        return initialTemperature;
    }

    /**
     * Setter for property 'initialTemperature'.
     *
     * @param initialTemperature Value to set for property 'initialTemperature'.
     */
    public void setInitialTemperature(DoubleParameter initialTemperature) {
        this.initialTemperature = initialTemperature;
    }

    /**
     * Getter for property 'temperatureDecay'.
     *
     * @return Value for property 'temperatureDecay'.
     */
    public DoubleParameter getTemperatureDecay() {
        return temperatureDecay;
    }

    /**
     * Setter for property 'temperatureDecay'.
     *
     * @param temperatureDecay Value to set for property 'temperatureDecay'.
     */
    public void setTemperatureDecay(DoubleParameter temperatureDecay) {
        this.temperatureDecay = temperatureDecay;
    }
}
