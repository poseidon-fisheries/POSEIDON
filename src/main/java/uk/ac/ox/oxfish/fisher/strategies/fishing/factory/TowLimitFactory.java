package uk.ac.ox.oxfish.fisher.strategies.fishing.factory;

import uk.ac.ox.oxfish.fisher.strategies.fishing.FishUntilFullStrategy;
import uk.ac.ox.oxfish.fisher.strategies.fishing.MaximumDaysDecorator;
import uk.ac.ox.oxfish.fisher.strategies.fishing.TowLimitFishingStrategy;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * Created by carrknight on 6/21/17.
 */
public class TowLimitFactory implements AlgorithmFactory<MaximumDaysDecorator> {


    private DoubleParameter towLimits = new FixedDoubleParameter(100);


    private DoubleParameter maxDaysOut = new FixedDoubleParameter(5);



    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public MaximumDaysDecorator apply(FishState state) {
        return
                new MaximumDaysDecorator(
                new TowLimitFishingStrategy(towLimits.apply(state.getRandom()).intValue()),
                maxDaysOut.apply(state.getRandom()).intValue()
                );
    }

    /**
     * Getter for property 'towLimits'.
     *
     * @return Value for property 'towLimits'.
     */
    public DoubleParameter getTowLimits() {
        return towLimits;
    }

    /**
     * Setter for property 'towLimits'.
     *
     * @param towLimits Value to set for property 'towLimits'.
     */
    public void setTowLimits(DoubleParameter towLimits) {
        this.towLimits = towLimits;
    }

    /**
     * Getter for property 'maxDaysOut'.
     *
     * @return Value for property 'maxDaysOut'.
     */
    public DoubleParameter getMaxDaysOut() {
        return maxDaysOut;
    }

    /**
     * Setter for property 'maxDaysOut'.
     *
     * @param maxDaysOut Value to set for property 'maxDaysOut'.
     */
    public void setMaxDaysOut(DoubleParameter maxDaysOut) {
        this.maxDaysOut = maxDaysOut;
    }
}
