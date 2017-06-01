package uk.ac.ox.oxfish.fisher.equipment.gear.factory;

import uk.ac.ox.oxfish.fisher.equipment.gear.Gear;
import uk.ac.ox.oxfish.fisher.equipment.gear.HoldLimitingDecoratorGear;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

/**
 * Created by carrknight on 6/1/17.
 */
public class HoldLimitingDecoratorFactory implements AlgorithmFactory<HoldLimitingDecoratorGear> {


    private AlgorithmFactory<? extends Gear> delegate = new FixedProportionGearFactory();


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public HoldLimitingDecoratorGear apply(FishState state) {
        return new HoldLimitingDecoratorGear(delegate.apply(state));
    }

    /**
     * Getter for property 'delegate'.
     *
     * @return Value for property 'delegate'.
     */
    public AlgorithmFactory<? extends Gear> getDelegate() {
        return delegate;
    }

    /**
     * Setter for property 'delegate'.
     *
     * @param delegate Value to set for property 'delegate'.
     */
    public void setDelegate(
            AlgorithmFactory<? extends Gear> delegate) {
        this.delegate = delegate;
    }
}
