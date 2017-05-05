package uk.ac.ox.oxfish.model.regs.factory;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.MultiQuotaRegulation;
import uk.ac.ox.oxfish.model.regs.WeakMultiQuotaRegulation;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.Locker;

import java.util.function.Supplier;

/**
 * Like the TAC String factory, but generates a "weak TAC" that is a TAC where you are allowed to go out
 * as long as at least one species of fish is available, but you are not allowed to sell it
 * Created by carrknight on 5/4/17.
 */
public class WeakMultiTACStringFactory implements AlgorithmFactory<WeakMultiQuotaRegulation>
{


    /**
     * The string we are going to turn into rule, "0:100 ,2:uniform 1 100" means that EACH FISHER gets 100 quotas a year
     * for species 0 and a random quota of 1 to 100 for species 2. The other species are then assumed NOT TO BE PROTECTED
     * by the quota (and can be fished out freely)
     */
    private String yearlyQuotaMaps = "0:500000";


    /**
     * for each model there is only one quota object being shared
     */
    private final Locker<FishState,WeakMultiQuotaRegulation> modelQuota = new Locker<>();



    @Override
    public WeakMultiQuotaRegulation apply(FishState state)
    {


        return modelQuota.presentKey(state,
                                     new Supplier<WeakMultiQuotaRegulation>() {
                                         @Override
                                         public WeakMultiQuotaRegulation get() {

                                             return new WeakMultiQuotaRegulation(
                                                     MultiTACStringFactory.turnStringIntoQuotaArray(
                                                             state,
                                                             yearlyQuotaMaps
                                                     ),
                                                     state);


                                         }
                                     });





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
