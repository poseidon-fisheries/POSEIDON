package uk.ac.ox.oxfish.model.regs.factory;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.MultiQuotaRegulation;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.Locker;

import java.util.function.Supplier;

/**
 * Created by carrknight on 7/19/17.
 */
public class MultiIQStringFactory implements AlgorithmFactory<MultiQuotaRegulation>
{


    /**
     * The string we are going to turn into rule, "0:100 ,2:uniform 1 100" means that ALL FISHERS gets 100 quotas a year
     * for species 0 and a random quota of 1 to 100 for species 2. The other species are then assumed NOT TO BE PROTECTED
     * by the quota (and can be fished out freely)
     */
    private String yearlyQuotaMaps = "0:500000";



    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public MultiQuotaRegulation apply(FishState state)
    {


        return  MultiTACStringFactory.createInstance(
                state, MultiIQStringFactory.this.yearlyQuotaMaps);
    }


    /**
     * Getter for property 'yearlyQuotaMaps'.
     *
     * @return Value for property 'yearlyQuotaMaps'.
     */
    public String getYearlyQuotaMaps() {
        return yearlyQuotaMaps;
    }

    /**
     * Setter for property 'yearlyQuotaMaps'.
     *
     * @param yearlyQuotaMaps Value to set for property 'yearlyQuotaMaps'.
     */
    public void setYearlyQuotaMaps(String yearlyQuotaMaps) {
        this.yearlyQuotaMaps = yearlyQuotaMaps;
    }
}
