package uk.ac.ox.oxfish.model.regs;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

/**
 * Like the original TAC, but you are allowed to go out as long as at least one quota is positive!
 * Created by carrknight on 5/3/17.
 */
public class WeakMultiQuotaRegulation extends MultiQuotaRegulation {
    public WeakMultiQuotaRegulation(double[] yearlyQuota, FishState state) {
        super(yearlyQuota, state);
    }


    @Override
    public boolean isFishingStillAllowed() {

        for(int i=0; i<getNumberOfSpeciesTracked(); i++)
            if(getQuotaRemaining(i)> FishStateUtilities.EPSILON)
                return true;

        return false;


    }
}
