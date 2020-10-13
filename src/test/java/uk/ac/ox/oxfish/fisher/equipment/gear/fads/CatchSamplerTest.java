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

package uk.ac.ox.oxfish.fisher.equipment.gear.fads;

import com.google.common.collect.ImmutableList;
import com.google.common.primitives.ImmutableDoubleArray;
import ec.util.MersenneTwisterFast;
import org.junit.Test;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.CatchSampler;

import java.util.Map;

import static com.google.common.primitives.ImmutableDoubleArray.of;
import static org.junit.Assert.assertEquals;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.entry;

public class CatchSamplerTest {

    @SuppressWarnings("UnstableApiUsage") @Test
    public void test() {
        final MersenneTwisterFast rng = new MersenneTwisterFast();
        final CatchSampler catchSampler = new CatchSampler(ImmutableList.of(
            ImmutableList.of(0.0, 0.0),
            ImmutableList.of(1.0, 1.0),
            ImmutableList.of(2.0, 2.0)
        ), rng);

        ImmutableList.<Map.Entry<ImmutableDoubleArray, ImmutableDoubleArray>>builder()
            .add(entry(of(0.0, 0.0), of(0.0, 0.0)))
            .add(entry(of(1.0, 1.0), of(1.0, 1.0)))
            .add(entry(of(2.0, 2.0), of(2.0, 2.0)))
            .add(entry(of(0.0, 0.0), of(0.0, 0.0)))
            .add(entry(of(0.5, 0.0), of(0.0, 0.0)))
            .add(entry(of(1.5, 1.5), of(1.0, 1.0)))
            .build()
            .forEach(entry -> {
                final ImmutableDoubleArray expectedCatch = entry.getValue();
                final double[] availableBiomass = entry.getKey().toArray();
                assertEquals(expectedCatch, catchSampler.next(availableBiomass));
            });

    }

}