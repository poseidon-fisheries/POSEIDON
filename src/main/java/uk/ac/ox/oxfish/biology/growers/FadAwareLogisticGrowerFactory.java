package uk.ac.ox.oxfish.biology.growers;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

public class FadAwareLogisticGrowerFactory implements AlgorithmFactory<FadAwareLogisticGrowerInitializer> {

    private DoubleParameter steepness = new FixedDoubleParameter(0.7);

    /**
     * when this is set to anything above 0, growth will be distributed with higher proportion to the area with higher
     * unfilled carrying capacity
     */
    private DoubleParameter distributionalWeight = new FixedDoubleParameter(-1);

    private boolean useLastYearBiomass = true;

    @SuppressWarnings("unused") public FadAwareLogisticGrowerFactory() { }

    public FadAwareLogisticGrowerFactory(double steepness) {
        this.steepness = new FixedDoubleParameter(steepness);
    }

    @SuppressWarnings("unused") public boolean getUseLastYearBiomass() { return useLastYearBiomass; }

    @SuppressWarnings("unused") public void setUseLastYearBiomass(final boolean useLastYearBiomass) {
        this.useLastYearBiomass = useLastYearBiomass;
    }

    @Override
    public FadAwareLogisticGrowerInitializer apply(FishState state) {
        return new FadAwareLogisticGrowerInitializer(
            steepness.makeCopy(),
            distributionalWeight.makeCopy(),
            useLastYearBiomass
        );
    }

    public DoubleParameter getSteepness() {
        return steepness;
    }

    public void setSteepness(DoubleParameter steepness) {
        this.steepness = steepness;
    }

    @SuppressWarnings("unused") public DoubleParameter getDistributionalWeight() {
        return distributionalWeight;
    }

    @SuppressWarnings("unused") public void setDistributionalWeight(DoubleParameter distributionalWeight) {
        this.distributionalWeight = distributionalWeight;
    }

}