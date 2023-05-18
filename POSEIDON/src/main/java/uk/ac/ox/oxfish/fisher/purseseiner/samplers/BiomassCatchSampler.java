/*
 * POSEIDON, an agent-based model of fisheries
 * Copyright (C) 2021 CoHESyS Lab cohesys.lab@gmail.com
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.fisher.purseseiner.samplers;

import com.google.common.primitives.ImmutableDoubleArray;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;

import java.util.Collection;

public class BiomassCatchSampler extends CatchSampler<BiomassLocalBiology> {

    public BiomassCatchSampler(
        final Collection<Collection<Double>> sample,
        final MersenneTwisterFast rng
    ) {
        super(sample, rng);
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    boolean test(
        final BiomassLocalBiology sourceBiology,
        final ImmutableDoubleArray catchArray
    ) {
        final double[] availableBiomass = sourceBiology.getCurrentBiomass();
        // Make sure that there is enough biomass for all species
        for (int i = 0; i < catchArray.length(); i++) {
            if (catchArray.get(i) > availableBiomass[i]) {
                return false;
            }
        }
        return true;
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public BiomassLocalBiology apply(final BiomassLocalBiology sourceBiology) {
        // reuse the biomassCaught array for carrying capacity, since it doesn't
        // make sense to have "extra" carrying capacity for a school of fish
        final double[] biomassCaught = next(sourceBiology).toArray();
        return new BiomassLocalBiology(biomassCaught, biomassCaught);
    }

}
