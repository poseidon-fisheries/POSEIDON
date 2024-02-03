package uk.ac.ox.oxfish.model.regs.policymakers.sensors;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

public class SimpleFishSamplerFactory implements AlgorithmFactory<SimpleFisherSampler> {


    private DoubleParameter percentageSampled = new FixedDoubleParameter(.025);

    @Override
    public SimpleFisherSampler apply(FishState fishState) {
        return new SimpleFisherSampler(
            percentageSampled.applyAsDouble(fishState.getRandom())
        );
    }


    public DoubleParameter getPercentageSampled() {
        return percentageSampled;
    }

    public void setPercentageSampled(DoubleParameter percentageSampled) {
        this.percentageSampled = percentageSampled;
    }
}
