package uk.ac.ox.oxfish.fisher.equipment.gear.factory;

import uk.ac.ox.oxfish.fisher.equipment.gear.DelayGearDecorator;
import uk.ac.ox.oxfish.fisher.equipment.gear.Gear;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

public class DelayGearDecoratorFactory implements AlgorithmFactory<DelayGearDecorator> {


    private DoubleParameter hoursItTake = new FixedDoubleParameter(12);

    private AlgorithmFactory<? extends Gear> delegate = new FixedProportionGearFactory();


    @Override
    public DelayGearDecorator apply(FishState fishState) {
        return new DelayGearDecorator(delegate.apply(fishState),
                hoursItTake.apply(fishState.getRandom()).intValue());
    }


    public DoubleParameter getHoursItTake() {
        return hoursItTake;
    }

    public void setHoursItTake(DoubleParameter hoursItTake) {
        this.hoursItTake = hoursItTake;
    }

    public AlgorithmFactory<? extends Gear> getDelegate() {
        return delegate;
    }

    public void setDelegate(AlgorithmFactory<? extends Gear> delegate) {
        this.delegate = delegate;
    }
}
