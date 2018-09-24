package uk.ac.ox.oxfish.biology.growers;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.UniformDoubleParameter;

public class CommonLogisticGrowerFactory implements AlgorithmFactory<CommonLogisticGrowerInitializer> {

    private DoubleParameter steepness = new FixedDoubleParameter(0.7);
    private boolean distributeProportionally = false;


    public CommonLogisticGrowerFactory() {
    }


    public CommonLogisticGrowerFactory(double steepness) {
        this.steepness = new FixedDoubleParameter(steepness);
    }


    public CommonLogisticGrowerFactory(double low,double high) {
        this.steepness =  new UniformDoubleParameter(low, high);
    }

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public CommonLogisticGrowerInitializer apply(FishState state) {
        return new CommonLogisticGrowerInitializer(steepness.makeCopy(), distributeProportionally);
    }

    /**
     * Getter for property 'steepness'.
     *
     * @return Value for property 'steepness'.
     */
    public DoubleParameter getSteepness() {
        return steepness;
    }

    /**
     * Setter for property 'steepness'.
     *
     * @param steepness Value to set for property 'steepness'.
     */
    public void setSteepness(DoubleParameter steepness) {
        this.steepness = steepness;
    }

    public boolean isDistributeProportionally() {
        return distributeProportionally;
    }

    public void setDistributeProportionally(boolean distributeProportionally) {
        this.distributeProportionally = distributeProportionally;
    }
}