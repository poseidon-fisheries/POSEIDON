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

import com.google.common.primitives.ImmutableDoubleArray;
import ec.util.MersenneTwisterFast;

import java.util.Collection;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.Streams.stream;

@SuppressWarnings("UnstableApiUsage")
public class CatchSampler {

    private final ConditionalSampler<ImmutableDoubleArray> sampler;

    public CatchSampler(final Collection<Collection<Double>> sample, MersenneTwisterFast rng) {
        this.sampler = new ConditionalSampler<>(
            stream(sample).map(ImmutableDoubleArray::copyOf).collect(toImmutableList()),
            rng
        );
    }

    public ImmutableDoubleArray next(double[] availableBiomass) {
        return sampler.next(catchArray -> {
            for (int i = 0; i < catchArray.length(); i++)
                if (catchArray.get(i) > availableBiomass[i]) return false;
            return true;
        });
    }

}
