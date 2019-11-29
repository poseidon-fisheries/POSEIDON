package uk.ac.ox.oxfish.fisher.strategies.fishing.factory;

import uk.ac.ox.oxfish.fisher.strategies.fishing.FadFishingStrategy;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

public class FadFishingStrategyFactory implements AlgorithmFactory<FadFishingStrategy> {
    private DoubleParameter fadDeploymentsCoefficient = new FixedDoubleParameter(0.01);
    private DoubleParameter setsOnOwnFadsCoefficient = new FixedDoubleParameter(0.01);
    private DoubleParameter setsOnOtherFadsCoefficient = new FixedDoubleParameter(0.01);
    private DoubleParameter unassociatedSetsCoefficient = new FixedDoubleParameter(1E-8);
    private DoubleParameter fadDeploymentsProbabilityDecay = new FixedDoubleParameter(0.01);
    private DoubleParameter fadSetsProbabilityDecay = new FixedDoubleParameter(0.01);
    private DoubleParameter unassociatedSetsProbabilityDecay = new FixedDoubleParameter(0.01);

    @SuppressWarnings("unused")
    public DoubleParameter getFadDeploymentsProbabilityDecay() {
        return fadDeploymentsProbabilityDecay;
    }

    @SuppressWarnings("unused")
    public void setFadDeploymentsProbabilityDecay(DoubleParameter fadDeploymentsProbabilityDecay) {
        this.fadDeploymentsProbabilityDecay = fadDeploymentsProbabilityDecay;
    }

    @SuppressWarnings("unused")
    public DoubleParameter getFadSetsProbabilityDecay() {
        return fadSetsProbabilityDecay;
    }

    @SuppressWarnings("unused")
    public void setFadSetsProbabilityDecay(DoubleParameter fadSetsProbabilityDecay) {
        this.fadSetsProbabilityDecay = fadSetsProbabilityDecay;
    }

    @SuppressWarnings("unused")
    public DoubleParameter getUnassociatedSetsProbabilityDecay() {
        return unassociatedSetsProbabilityDecay;
    }

    @SuppressWarnings("unused")
    public void setUnassociatedSetsProbabilityDecay(DoubleParameter unassociatedSetsProbabilityDecay) {
        this.unassociatedSetsProbabilityDecay = unassociatedSetsProbabilityDecay;
    }

    @SuppressWarnings("unused")
    public DoubleParameter getUnassociatedSetsCoefficient() {
        return unassociatedSetsCoefficient;
    }

    @SuppressWarnings("unused")
    public void setUnassociatedSetsCoefficient(DoubleParameter unassociatedSetsCoefficient) {
        this.unassociatedSetsCoefficient = unassociatedSetsCoefficient;
    }

    @SuppressWarnings("unused")
    public DoubleParameter getFadDeploymentsCoefficient() {
        return fadDeploymentsCoefficient;
    }

    @SuppressWarnings("unused")
    public void setFadDeploymentsCoefficient(DoubleParameter fadDeploymentsCoefficient) {
        this.fadDeploymentsCoefficient = fadDeploymentsCoefficient;
    }

    @SuppressWarnings("unused")
    public DoubleParameter getSetsOnOwnFadsCoefficient() {
        return setsOnOwnFadsCoefficient;
    }

    @SuppressWarnings("unused")
    public void setSetsOnOwnFadsCoefficient(DoubleParameter setsOnOwnFadsCoefficient) {
        this.setsOnOwnFadsCoefficient = setsOnOwnFadsCoefficient;
    }

    @SuppressWarnings("unused")
    public DoubleParameter getSetsOnOtherFadsCoefficient() {
        return setsOnOtherFadsCoefficient;
    }

    @SuppressWarnings("unused")
    public void setSetsOnOtherFadsCoefficient(DoubleParameter setsOnOtherFadsCoefficient) {
        this.setsOnOtherFadsCoefficient = setsOnOtherFadsCoefficient;
    }

    @Override
    public FadFishingStrategy apply(FishState fishState) {
        return new FadFishingStrategy(
            unassociatedSetsCoefficient.apply(fishState.random),
            fadDeploymentsCoefficient.apply(fishState.random),
            setsOnOwnFadsCoefficient.apply(fishState.random),
            setsOnOtherFadsCoefficient.apply(fishState.random),
            fadDeploymentsProbabilityDecay.apply(fishState.random),
            fadSetsProbabilityDecay.apply(fishState.random),
            unassociatedSetsProbabilityDecay.apply(fishState.random)
        );
    }
}
