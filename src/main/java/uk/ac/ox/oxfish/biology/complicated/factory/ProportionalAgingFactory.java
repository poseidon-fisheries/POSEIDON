package uk.ac.ox.oxfish.biology.complicated.factory;

import uk.ac.ox.oxfish.biology.complicated.ProportionalAgingProcess;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * Created by carrknight on 7/7/17.
 */
public class ProportionalAgingFactory implements AlgorithmFactory<ProportionalAgingProcess> {

    /**
     * generates a number between 0 and 1 (the method bounds it so otherwise) representing
     * how many fish of class x move between one bin and the next
     */
    private DoubleParameter proportionAging = new FixedDoubleParameter(.2);


    /**
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public ProportionalAgingProcess apply(FishState fishState) {
        return new ProportionalAgingProcess(proportionAging.makeCopy());
    }

    /**
     * Getter for property 'proportionAging'.
     *
     * @return Value for property 'proportionAging'.
     */
    public DoubleParameter getProportionAging() {
        return proportionAging;
    }

    /**
     * Setter for property 'proportionAging'.
     *
     * @param proportionAging Value to set for property 'proportionAging'.
     */
    public void setProportionAging(DoubleParameter proportionAging) {
        this.proportionAging = proportionAging;
    }
}
