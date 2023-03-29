package uk.ac.ox.oxfish.model.plugins;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

public class IattcClosurePeriodRandomizerFactory implements AlgorithmFactory<IattcClosurePeriodRandomizer> {

    DoubleParameter proportionOfBoatsInClosureA = new FixedDoubleParameter(0.5);

    public DoubleParameter getProportionOfBoatsInClosureA() {
        return proportionOfBoatsInClosureA;
    }

    @SuppressWarnings("unused")
    public void setProportionOfBoatsInClosureA(final DoubleParameter proportionOfBoatsInClosureA) {
        this.proportionOfBoatsInClosureA = proportionOfBoatsInClosureA;
    }

    @Override
    public IattcClosurePeriodRandomizer apply(final FishState fishState) {
        return new IattcClosurePeriodRandomizer(proportionOfBoatsInClosureA.applyAsDouble(fishState.getRandom()));
    }
}
