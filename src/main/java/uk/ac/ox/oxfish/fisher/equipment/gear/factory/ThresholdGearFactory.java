package uk.ac.ox.oxfish.fisher.equipment.gear.factory;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.equipment.gear.HomogeneousAbundanceGear;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.CutoffAbundanceFilter;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.FixedProportionFilter;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * An abundance based gear, catching fish only above/below a length quota
 * Created by carrknight on 3/11/16.
 */
public class ThresholdGearFactory implements AlgorithmFactory<HomogeneousAbundanceGear>
{

    private DoubleParameter threshold = new FixedDoubleParameter(10d);

    private DoubleParameter litersGasPerHourFished = new FixedDoubleParameter(5);

    private DoubleParameter catchability = new FixedDoubleParameter(0.01d);

    private boolean selectAboveThreshold = true;


    /**
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public HomogeneousAbundanceGear apply(FishState fishState) {
        MersenneTwisterFast random = fishState.getRandom();
        return  new HomogeneousAbundanceGear(litersGasPerHourFished.apply(random),
                                             new FixedProportionFilter(catchability.apply(random)),
                                             new CutoffAbundanceFilter(threshold.apply(random),
                                                                       selectAboveThreshold));
    }



    public boolean isSelectAboveThreshold() {
        return selectAboveThreshold;
    }

    public void setSelectAboveThreshold(boolean selectAboveThreshold) {
        this.selectAboveThreshold = selectAboveThreshold;
    }

    /**
     * Getter for property 'threshold'.
     *
     * @return Value for property 'threshold'.
     */
    public DoubleParameter getThreshold() {
        return threshold;
    }

    /**
     * Setter for property 'threshold'.
     *
     * @param threshold Value to set for property 'threshold'.
     */
    public void setThreshold(DoubleParameter threshold) {
        this.threshold = threshold;
    }

    /**
     * Getter for property 'litersGasPerHourFished'.
     *
     * @return Value for property 'litersGasPerHourFished'.
     */
    public DoubleParameter getLitersGasPerHourFished() {
        return litersGasPerHourFished;
    }

    /**
     * Setter for property 'litersGasPerHourFished'.
     *
     * @param litersGasPerHourFished Value to set for property 'litersGasPerHourFished'.
     */
    public void setLitersGasPerHourFished(DoubleParameter litersGasPerHourFished) {
        this.litersGasPerHourFished = litersGasPerHourFished;
    }

    /**
     * Getter for property 'catchability'.
     *
     * @return Value for property 'catchability'.
     */
    public DoubleParameter getCatchability() {
        return catchability;
    }

    /**
     * Setter for property 'catchability'.
     *
     * @param catchability Value to set for property 'catchability'.
     */
    public void setCatchability(DoubleParameter catchability) {
        this.catchability = catchability;
    }
}
