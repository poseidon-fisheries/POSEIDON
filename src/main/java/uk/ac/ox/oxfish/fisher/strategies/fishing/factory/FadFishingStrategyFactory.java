package uk.ac.ox.oxfish.fisher.strategies.fishing.factory;

import uk.ac.ox.oxfish.fisher.strategies.fishing.FadFishingStrategy;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

public class FadFishingStrategyFactory implements AlgorithmFactory<FadFishingStrategy> {
    DoubleParameter fadsDeploymentCoefficient = new FixedDoubleParameter(0.01);
    DoubleParameter ownFadsSettingCoefficient = new FixedDoubleParameter(0.01);
    DoubleParameter otherFadsSettingCoefficient = new FixedDoubleParameter(0.01);
    DoubleParameter probabilityDecayCoefficient = new FixedDoubleParameter(0.01);

    public DoubleParameter getFadsDeploymentCoefficient() {
        return fadsDeploymentCoefficient;
    }

    public void setFadsDeploymentCoefficient(DoubleParameter fadsDeploymentCoefficient) {
        this.fadsDeploymentCoefficient = fadsDeploymentCoefficient;
    }

    public DoubleParameter getOwnFadsSettingCoefficient() {
        return ownFadsSettingCoefficient;
    }

    public void setOwnFadsSettingCoefficient(DoubleParameter ownFadsSettingCoefficient) {
        this.ownFadsSettingCoefficient = ownFadsSettingCoefficient;
    }

    public DoubleParameter getOtherFadsSettingCoefficient() {
        return otherFadsSettingCoefficient;
    }

    public void setOtherFadsSettingCoefficient(DoubleParameter otherFadsSettingCoefficient) {
        this.otherFadsSettingCoefficient = otherFadsSettingCoefficient;
    }

    @Override
    public FadFishingStrategy apply(FishState fishState) {
        return new FadFishingStrategy(
            fadsDeploymentCoefficient.apply(fishState.random),
            ownFadsSettingCoefficient.apply(fishState.random),
            otherFadsSettingCoefficient.apply(fishState.random),
            probabilityDecayCoefficient.apply(fishState.random)
        );
    }
}
