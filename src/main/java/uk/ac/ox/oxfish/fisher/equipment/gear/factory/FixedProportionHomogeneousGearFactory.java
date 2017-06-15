package uk.ac.ox.oxfish.fisher.equipment.gear.factory;

import uk.ac.ox.oxfish.fisher.equipment.gear.HomogeneousAbundanceGear;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.FixedProportionFilter;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * Created by carrknight on 6/12/17.
 */
public class FixedProportionHomogeneousGearFactory implements HomogeneousGearFactory {


    private DoubleParameter catchability = new FixedDoubleParameter(.0001);

    private DoubleParameter litersOfGasConsumed = new FixedDoubleParameter(0d);

    /**
     * Getter for property 'averageCatchability'.
     *
     * @return Value for property 'averageCatchability'.
     */
    @Override
    public DoubleParameter getAverageCatchability() {
        return catchability;
    }

    /**
     * Setter for property 'averageCatchability'.
     *
     * @param averageCatchability Value to set for property 'averageCatchability'.
     */
    @Override
    public void setAverageCatchability(DoubleParameter averageCatchability) {
        this.catchability = averageCatchability;
    }

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public HomogeneousAbundanceGear apply(FishState state) {

        return new HomogeneousAbundanceGear(
                litersOfGasConsumed.apply(state.getRandom()),
                new FixedProportionFilter(getAverageCatchability().apply(state.getRandom()))
        );
    }


    /**
     * Getter for property 'litersOfGasConsumed'.
     *
     * @return Value for property 'litersOfGasConsumed'.
     */
    public DoubleParameter getLitersOfGasConsumed() {
        return litersOfGasConsumed;
    }

    /**
     * Setter for property 'litersOfGasConsumed'.
     *
     * @param litersOfGasConsumed Value to set for property 'litersOfGasConsumed'.
     */
    public void setLitersOfGasConsumed(DoubleParameter litersOfGasConsumed) {
        this.litersOfGasConsumed = litersOfGasConsumed;
    }
}
