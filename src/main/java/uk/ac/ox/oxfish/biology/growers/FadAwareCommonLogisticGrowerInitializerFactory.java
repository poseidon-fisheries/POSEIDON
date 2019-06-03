package uk.ac.ox.oxfish.biology.growers;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.UniformDoubleParameter;

public class FadAwareCommonLogisticGrowerInitializerFactory implements AlgorithmFactory<FadAwareCommonLogisticGrowerInitializer> {

    private DoubleParameter steepness = new FixedDoubleParameter(0.7);
    /**
     * when this is set to anything above 0, growth will be distributed with higher proportion to the area with higher
     * unfilled carrying capacity
     */
    private DoubleParameter distributionalWeight = new FixedDoubleParameter(-1);

    public FadAwareCommonLogisticGrowerInitializerFactory() {
    }

    public FadAwareCommonLogisticGrowerInitializerFactory(double steepness) {
        this.steepness = new FixedDoubleParameter(steepness);
    }

    public FadAwareCommonLogisticGrowerInitializerFactory(double low, double high) {
        this.steepness = new UniformDoubleParameter(low, high);
    }

    @Override
    public FadAwareCommonLogisticGrowerInitializer apply(FishState state) {
        return new FadAwareCommonLogisticGrowerInitializer(
            steepness.makeCopy(),
            distributionalWeight.apply(state.getRandom())
        );
    }

    public DoubleParameter getSteepness() {
        return steepness;
    }

    public void setSteepness(DoubleParameter steepness) {
        this.steepness = steepness;
    }

    public DoubleParameter getDistributionalWeight() {
        return distributionalWeight;
    }

    public void setDistributionalWeight(DoubleParameter distributionalWeight) {
        this.distributionalWeight = distributionalWeight;
    }
}