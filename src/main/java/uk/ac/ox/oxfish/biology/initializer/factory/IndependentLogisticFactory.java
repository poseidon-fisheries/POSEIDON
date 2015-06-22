package uk.ac.ox.oxfish.biology.initializer.factory;

import uk.ac.ox.oxfish.biology.initializer.BiologyInitializer;
import uk.ac.ox.oxfish.biology.initializer.IndependentLogisticInitializer;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.StrategyFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * Factory for IndependentLogisticInitializer
 * Created by carrknight on 6/22/15.
 */
public class IndependentLogisticFactory implements StrategyFactory<BiologyInitializer>
{
    private DoubleParameter carryingCapacity = new FixedDoubleParameter(5000);

    private double minSteepness = 0.6;

    private double maxSteepness=0.8;


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public BiologyInitializer apply(FishState state) {
        return new IndependentLogisticInitializer(carryingCapacity.apply(state.random),
                                                  minSteepness,maxSteepness);
    }

    public DoubleParameter getCarryingCapacity() {
        return carryingCapacity;
    }

    public void setCarryingCapacity(DoubleParameter carryingCapacity) {
        this.carryingCapacity = carryingCapacity;
    }

    public double getMinSteepness() {
        return minSteepness;
    }

    public void setMinSteepness(double minSteepness) {
        this.minSteepness = minSteepness;
    }

    public double getMaxSteepness() {
        return maxSteepness;
    }

    public void setMaxSteepness(double maxSteepness) {
        this.maxSteepness = maxSteepness;
    }
}
