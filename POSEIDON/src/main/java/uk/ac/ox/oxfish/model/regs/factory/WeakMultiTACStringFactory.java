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

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.Gatherer;
import uk.ac.ox.oxfish.model.regs.WeakMultiQuotaRegulation;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

/**
 * Like the TAC String factory, but generates a "weak TAC" that is a TAC where you are allowed to go out
 * as long as at least one species of fish is available, but you are not allowed to sell it
 * Created by carrknight on 5/4/17.
 */
public class WeakMultiTACStringFactory implements AlgorithmFactory<WeakMultiQuotaRegulation> {


    /**
     * for each model there is only one quota object being shared
     */
    @SuppressWarnings("deprecation")
    private final uk.ac.ox.oxfish.utility.Locker<String, WeakMultiQuotaRegulation> modelQuota =
        new uk.ac.ox.oxfish.utility.Locker<>();
    /**
     * The string we are going to turn into rule, "0:100 ,2:uniform 1 100" means that EACH FISHER gets 100 quotas a year
     * for species 0 and a random quota of 1 to 100 for species 2. The other species are then assumed NOT TO BE PROTECTED
     * by the quota (and can be fished out freely)
     */
    private String yearlyQuotaMaps = "0:500000";

    @Override
    public WeakMultiQuotaRegulation apply(final FishState state) {


        return modelQuota.presentKey(
            state.getUniqueID(),
            () -> {

                final WeakMultiQuotaRegulation quotas = new WeakMultiQuotaRegulation(
                    MultiTACStringFactory.turnStringIntoQuotaArray(
                        state,
                        yearlyQuotaMaps
                    ),
                    state
                );

                for (final Species species : state.getSpecies()) {
                    state.getYearlyDataSet().registerGatherer(
                        "Last Season Day of " + species,
                        (Gatherer<FishState>) state1 -> (double) quotas.getLastSeasonDay()[species.getIndex()],
                        365d
                    );
                }

                return quotas;


            }
        );


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
    public void setYearlyQuotaMaps(final String yearlyQuotaMaps) {
        this.yearlyQuotaMaps = yearlyQuotaMaps;
    }
}
