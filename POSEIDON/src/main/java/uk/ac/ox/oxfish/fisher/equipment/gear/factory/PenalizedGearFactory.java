package uk.ac.ox.oxfish.fisher.equipment.gear.factory;

import uk.ac.ox.oxfish.fisher.equipment.gear.Gear;
import uk.ac.ox.oxfish.fisher.equipment.gear.PenalizedGear;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

public class PenalizedGearFactory implements AlgorithmFactory<PenalizedGear> {

    private DoubleParameter percentageCatchLost = new FixedDoubleParameter(.1);

    private AlgorithmFactory<? extends Gear> delegate = new FixedProportionGearFactory();

    @Override
    public PenalizedGear apply(final FishState fishState) {
        return new PenalizedGear(
            percentageCatchLost.applyAsDouble(fishState.getRandom()),
            delegate.apply(fishState)
        );
    }

    public DoubleParameter getPercentageCatchLost() {
        return percentageCatchLost;
    }

    public void setPercentageCatchLost(final DoubleParameter percentageCatchLost) {
        this.percentageCatchLost = percentageCatchLost;
    }

    public AlgorithmFactory<? extends Gear> getDelegate() {
        return delegate;
    }

    public void setDelegate(final AlgorithmFactory<? extends Gear> delegate) {
        this.delegate = delegate;
    }
}
