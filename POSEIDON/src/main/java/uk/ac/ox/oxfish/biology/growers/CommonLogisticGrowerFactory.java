package uk.ac.ox.oxfish.biology.growers;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.UniformDoubleParameter;

public class CommonLogisticGrowerFactory implements AlgorithmFactory<CommonLogisticGrowerInitializer> {

    private DoubleParameter steepness = new FixedDoubleParameter(0.7);
    /**
     * when this is set to anything above 0, growth will be distributed with higher proportion to the area with higher
     * unfilled carrying capacity
     */
    private DoubleParameter distributionalWeight = new FixedDoubleParameter(-1);

    public CommonLogisticGrowerFactory() {
    }

    public CommonLogisticGrowerFactory(final double steepness) {
        this.steepness = new FixedDoubleParameter(steepness);
    }

    public CommonLogisticGrowerFactory(
        final double low,
        final double high
    ) {
        this.steepness = new UniformDoubleParameter(low, high);
    }

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public CommonLogisticGrowerInitializer apply(final FishState state) {
        return new CommonLogisticGrowerInitializer(
            steepness.makeCopy(),
            distributionalWeight.applyAsDouble(state.getRandom())
        );
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
    public void setSteepness(final DoubleParameter steepness) {
        this.steepness = steepness;
    }

    /**
     * Getter for property 'distributionalWeight'.
     *
     * @return Value for property 'distributionalWeight'.
     */
    public DoubleParameter getDistributionalWeight() {
        return distributionalWeight;
    }

    /**
     * Setter for property 'distributionalWeight'.
     *
     * @param distributionalWeight Value to set for property 'distributionalWeight'.
     */
    public void setDistributionalWeight(final DoubleParameter distributionalWeight) {
        this.distributionalWeight = distributionalWeight;
    }

}
