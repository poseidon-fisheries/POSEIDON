package uk.ac.ox.oxfish.fisher.strategies.fishing.factory;

import uk.ac.ox.oxfish.fisher.strategies.fishing.FadFishingStrategy;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

public class FadFishingStrategyFactory implements AlgorithmFactory<FadFishingStrategy> {
    private DoubleParameter unassociatedSetCoefficient = new FixedDoubleParameter(1E-8);
    private DoubleParameter fadsDeploymentCoefficient = new FixedDoubleParameter(0.01);
    private DoubleParameter ownFadsSettingCoefficient = new FixedDoubleParameter(0.01);
    private DoubleParameter otherFadsSettingCoefficient = new FixedDoubleParameter(0.01);
    private DoubleParameter probabilityDecayCoefficient = new FixedDoubleParameter(0.01);

    @SuppressWarnings("unused")
    public DoubleParameter getUnassociatedSetCoefficient() {
        return unassociatedSetCoefficient;
    }

    @SuppressWarnings("unused")
    public void setUnassociatedSetCoefficient(DoubleParameter unassociatedSetCoefficient) {
        this.unassociatedSetCoefficient = unassociatedSetCoefficient;
    }

    @SuppressWarnings("unused")
    public DoubleParameter getProbabilityDecayCoefficient() {
        return probabilityDecayCoefficient;
    }

    @SuppressWarnings("unused")
    public void setProbabilityDecayCoefficient(DoubleParameter probabilityDecayCoefficient) {
        this.probabilityDecayCoefficient = probabilityDecayCoefficient;
    }

    @SuppressWarnings("unused")
    public DoubleParameter getFadsDeploymentCoefficient() {
        return fadsDeploymentCoefficient;
    }

    @SuppressWarnings("unused")
    public void setFadsDeploymentCoefficient(DoubleParameter fadsDeploymentCoefficient) {
        this.fadsDeploymentCoefficient = fadsDeploymentCoefficient;
    }

    @SuppressWarnings("unused")
    public DoubleParameter getOwnFadsSettingCoefficient() {
        return ownFadsSettingCoefficient;
    }

    @SuppressWarnings("unused")
    public void setOwnFadsSettingCoefficient(DoubleParameter ownFadsSettingCoefficient) {
        this.ownFadsSettingCoefficient = ownFadsSettingCoefficient;
    }

    @SuppressWarnings("unused")
    public DoubleParameter getOtherFadsSettingCoefficient() {
        return otherFadsSettingCoefficient;
    }

    @SuppressWarnings("unused")
    public void setOtherFadsSettingCoefficient(DoubleParameter otherFadsSettingCoefficient) {
        this.otherFadsSettingCoefficient = otherFadsSettingCoefficient;
    }

    @Override
    public FadFishingStrategy apply(FishState fishState) {
        return new FadFishingStrategy(
            unassociatedSetCoefficient.apply(fishState.random),
            fadsDeploymentCoefficient.apply(fishState.random),
            ownFadsSettingCoefficient.apply(fishState.random),
            otherFadsSettingCoefficient.apply(fishState.random),
            probabilityDecayCoefficient.apply(fishState.random)
        );
    }
}
