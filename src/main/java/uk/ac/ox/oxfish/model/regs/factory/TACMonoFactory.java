/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.model.regs.factory;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.MonoQuotaRegulation;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.Locker;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

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
    private final Locker<FishState,MonoQuotaRegulation> modelQuota = new Locker<>();


    /**
     * the quota to use
     */
    private DoubleParameter quota = new FixedDoubleParameter(500000);


    /**
     * Creates a TAC and optionally the whole structure that keeps track of opportunity costs
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    @SuppressWarnings("unchecked")
    public MonoQuotaRegulation apply(FishState state) {


        final Double yearlyQuota = quota.apply(state.random);
        final MonoQuotaRegulation quotaRegulation =
                modelQuota.presentKey(state, () -> new MonoQuotaRegulation(yearlyQuota));

        //if it has not been consumed (probably because the model still has to start) then:
        if(quotaRegulation.getQuotaRemaining(0) > 0 &&
                Math.abs(quotaRegulation.getQuotaRemaining(0)-quotaRegulation.getYearlyQuota())<.1)
            quotaRegulation.setQuotaRemaining(0, yearlyQuota);

        //set yearly quota (notice that this will affect everyone)
        quotaRegulation.setYearlyQuota(yearlyQuota);

        //don't let quota remaining be above yearly quota though
        if(quotaRegulation.getQuotaRemaining(0) > quotaRegulation.getYearlyQuota())
            quotaRegulation.setQuotaRemaining(0, yearlyQuota);




        return quotaRegulation;

    }

    public DoubleParameter getQuota() {
        return quota;
    }


    public void setQuota(DoubleParameter quota) {
        this.quota = quota;
    }


}

