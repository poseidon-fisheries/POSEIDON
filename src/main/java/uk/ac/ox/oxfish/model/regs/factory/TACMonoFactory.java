package uk.ac.ox.oxfish.model.regs.factory;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.MonoQuotaRegulation;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.HashMap;
import java.util.Map;

/**
 * Creates a single mono-quota object and shares it every time it is called. If you modify the quota parameter here, it will
 * affect all the quotas that were created before as well
 * Created by carrknight on 6/14/15.
 */
public class TACMonoFactory implements AlgorithmFactory<MonoQuotaRegulation>
{

    /**
     * for each model there is only one quota object being shared
     */
    private final Map<FishState,MonoQuotaRegulation> modelQuota = new HashMap<>();


    /**
     * the quota to use
     */
    private DoubleParameter quota = new FixedDoubleParameter(1000);

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public MonoQuotaRegulation apply(FishState state) {


        final Double yearlyQuota = quota.apply(state.random);
        modelQuota.putIfAbsent(state,new MonoQuotaRegulation(yearlyQuota,state));
        final MonoQuotaRegulation quotaRegulation = modelQuota.get(state);
        //if it has not been consumed (probably because the model still has to start) then:
        if(quotaRegulation.getQuotaRemaining() > 0 &&
                Math.abs(quotaRegulation.getQuotaRemaining()-quotaRegulation.getYearlyQuota())<.1)
            quotaRegulation.setQuotaRemaining(yearlyQuota);

        //set yearly quota (notice that this will affect everyone)
        quotaRegulation.setYearlyQuota(yearlyQuota);

        //don't let quota remaining be above yearly quota though
        if(quotaRegulation.getQuotaRemaining() > quotaRegulation.getYearlyQuota())
            quotaRegulation.setQuotaRemaining(yearlyQuota);

        return quotaRegulation;

    }

    public DoubleParameter getQuota() {
        return quota;
    }


    public void setQuota(DoubleParameter quota) {
        this.quota = quota;
    }
}

