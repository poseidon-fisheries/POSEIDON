/*
 *  POSEIDON, an agent-based model of fisheries
 *  Copyright (C) 2020  CoHESyS Lab cohesys.lab@gmail.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package uk.ac.ox.oxfish.fisher.purseseiner.samplers;

import static com.google.common.collect.ImmutableList.toImmutableList;

import com.google.common.primitives.ImmutableDoubleArray;
import ec.util.MersenneTwisterFast;
import java.util.Collection;
import java.util.function.UnaryOperator;
import uk.ac.ox.oxfish.biology.LocalBiology;

@SuppressWarnings("UnstableApiUsage")
public abstract class CatchSampler<B extends LocalBiology> implements UnaryOperator<B> {

    private final ConditionalSampler<ImmutableDoubleArray> sampler;

    public CatchSampler(
        final Collection<Collection<Double>> sample,
        final MersenneTwisterFast rng
    ) {
        this.sampler = new ConditionalSampler<>(
            sample.stream().map(ImmutableDoubleArray::copyOf).collect(toImmutableList()),
            rng
        );
    }

    public ImmutableDoubleArray next(final double[] availableBiomass) {
        return sampler.next(catchArray -> {
            // Make sure that there is enough biomass for all species
            for (int i = 0; i < catchArray.length(); i++) {
                if (catchArray.get(i) > availableBiomass[i]) {
                    return false;
                }
            }
            return true;
        });
    }

}
