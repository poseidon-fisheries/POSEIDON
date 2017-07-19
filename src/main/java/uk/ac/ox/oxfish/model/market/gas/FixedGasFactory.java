package uk.ac.ox.oxfish.model.market.gas;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * Created by carrknight on 7/18/17.
 */
public class FixedGasFactory implements AlgorithmFactory<FixedGasPrice> {

    private DoubleParameter gasPrice = new FixedDoubleParameter(0.01);


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public FixedGasPrice apply(FishState state) {

        return new FixedGasPrice(gasPrice.apply(state.getRandom()));

    }

    public FixedGasFactory() {
    }


    public FixedGasFactory(double gasPrice) {
        this.gasPrice = new FixedDoubleParameter(gasPrice);
    }

    /**
     * Getter for property 'gasPrice'.
     *
     * @return Value for property 'gasPrice'.
     */
    public DoubleParameter getGasPrice() {
        return gasPrice;
    }

    /**
     * Setter for property 'gasPrice'.
     *
     * @param gasPrice Value to set for property 'gasPrice'.
     */
    public void setGasPrice(DoubleParameter gasPrice) {
        this.gasPrice = gasPrice;
    }
}
