/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.model.regs.factory;


import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.MultiQuotaRegulation;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.yaml.YamlConstructor;

import java.util.Arrays;
import java.util.Map;

public class MultiTACStringFactory implements AlgorithmFactory<MultiQuotaRegulation> {


    /**
     * for each model there is only one quota object being shared
     */
    @SuppressWarnings("deprecation")
    private final uk.ac.ox.oxfish.utility.Locker<String, MultiQuotaRegulation> modelQuota =
        new uk.ac.ox.oxfish.utility.Locker<>();
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
    public MultiQuotaRegulation apply(final FishState state) {


        return modelQuota.presentKey(
            state.getUniqueID(),
            () -> createInstance(state, MultiTACStringFactory.this.yearlyQuotaMaps)
        );


    }

    public static MultiQuotaRegulation createInstance(
        final FishState state, final String yearlyQuotaMaps
    ) {

        final double[] quotas = turnStringIntoQuotaArray(state, yearlyQuotaMaps);

        final MultiQuotaRegulation regulations = new MultiQuotaRegulation(quotas, state);
        //now create the opportunity costs manager
        //  TACOpportunityCostManager manager = new TACOpportunityCostManager(regulations);
        //  state.registerStartable(manager);

        return regulations;
    }

    public static double[] turnStringIntoQuotaArray(final FishState state, final String yearlyQuotaMaps) {
        final Map<String, String> quotasInputted = Splitter.on(",")
            .withKeyValueSeparator(":")
            .split(yearlyQuotaMaps.trim());
        Preconditions.checkArgument(quotasInputted.size() > 0, "You provided no quota for the TAC!");

        //here we store the quotas
        final double[] quotas = new double[state.getSpecies().size()];
        //start them as non-binding
        Arrays.fill(quotas, Double.POSITIVE_INFINITY);
        //go for each input and read the results
        for (final Map.Entry<String, String> input : quotasInputted.entrySet()) {
            final Double yearlyQuota = YamlConstructor.parseDoubleParameter(input.getValue().trim())
                .applyAsDouble(state.getRandom());
            Preconditions.checkArgument(yearlyQuota > 0, "quotas must start above 0!");
            Preconditions.checkArgument(!yearlyQuota.isNaN());
            quotas[Integer.parseInt(input.getKey().trim())] = yearlyQuota;
        }
        return quotas;
    }

    public String getYearlyQuotaMaps() {
        return yearlyQuotaMaps;
    }

    public void setYearlyQuotaMaps(final String yearlyQuotaMaps) {
        this.yearlyQuotaMaps = yearlyQuotaMaps;
    }

}
