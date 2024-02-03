package uk.ac.ox.oxfish.fisher.equipment.gear.factory;

import uk.ac.ox.oxfish.fisher.equipment.gear.Gear;
import uk.ac.ox.oxfish.fisher.equipment.gear.MaxThroughputDecorator;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

public class MaxThroughputDecoratorFactory implements AlgorithmFactory<MaxThroughputDecorator> {

    private AlgorithmFactory<? extends Gear> delegate = new FixedProportionGearFactory();

    private DoubleParameter maxThroughput = new FixedDoubleParameter(400);


    @Override
    public MaxThroughputDecorator apply(final FishState fishState) {
        return new MaxThroughputDecorator(
            delegate.apply(fishState),
            maxThroughput.applyAsDouble(fishState.getRandom())
        );
    }

    public AlgorithmFactory<? extends Gear> getDelegate() {
        return delegate;
    }

    public void setDelegate(final AlgorithmFactory<? extends Gear> delegate) {
        this.delegate = delegate;
    }

    public DoubleParameter getMaxThroughput() {
        return maxThroughput;
    }

    public void setMaxThroughput(final DoubleParameter maxThroughput) {
        this.maxThroughput = maxThroughput;
    }
}
