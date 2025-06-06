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

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.MultiQuotaRegulation;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

import java.util.HashMap;
import java.util.Map;

/**
 * Multiquota factory, including TAC Opportunity Cost manager
 * Created by carrknight on 10/20/15.
 */
public class TACMultiFactory implements AlgorithmFactory<MultiQuotaRegulation> {

    /**
     * for each model there is only one quota object being shared
     */
    private final Map<FishState, MultiQuotaRegulation> modelQuota = new HashMap<>();


    /**
     * the quota for first species
     */
    private DoubleParameter firstSpeciesQuota = new FixedDoubleParameter(500000);

    /**
     * the quota for any other species
     */
    private DoubleParameter otherSpeciesQuota = new FixedDoubleParameter(500000);


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public MultiQuotaRegulation apply(final FishState state) {

        if (!modelQuota.containsKey(state))
            modelQuota.put(state, createInstance(state.getRandom(), state.getSpecies().size(), state));

        return modelQuota.get(state);


    }


    private MultiQuotaRegulation createInstance(
        final MersenneTwisterFast random, final int numberOfSpecies,
        final FishState state
    ) {

        final double[] quotas = new double[numberOfSpecies];
        quotas[0] = firstSpeciesQuota.applyAsDouble(random);

        for (int i = 1; i < numberOfSpecies; i++) {
            quotas[i] = otherSpeciesQuota.applyAsDouble(random);
        }
        final MultiQuotaRegulation regulations = new MultiQuotaRegulation(quotas, state);
        //now create the opportunity costs manager
        //   TACOpportunityCostManager manager = new TACOpportunityCostManager(regulations);
        //    state.registerStartable(manager);


        return regulations;
    }

    public DoubleParameter getFirstSpeciesQuota() {
        return firstSpeciesQuota;
    }

    public void setFirstSpeciesQuota(final DoubleParameter firstSpeciesQuota) {
        this.firstSpeciesQuota = firstSpeciesQuota;
    }

    public DoubleParameter getOtherSpeciesQuota() {
        return otherSpeciesQuota;
    }

    public void setOtherSpeciesQuota(final DoubleParameter otherSpeciesQuota) {
        this.otherSpeciesQuota = otherSpeciesQuota;
    }
}
