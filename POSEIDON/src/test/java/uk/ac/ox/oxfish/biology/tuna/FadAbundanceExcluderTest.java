/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

package uk.ac.ox.oxfish.biology.tuna;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.SpeciesCodes;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntFunction;

import static uk.ac.ox.oxfish.biology.tuna.SmallLargeAllocationGridsSupplier.SizeGroup.LARGE;
import static uk.ac.ox.oxfish.biology.tuna.SmallLargeAllocationGridsSupplier.SizeGroup.SMALL;

public class FadAbundanceExcluderTest {

    @Test
    public void FadAbundanceExcluderTester() {

        final Species species1 = new Species("Piano Tuna");

        final Map<String, String> sCodes = new HashMap<>();
        sCodes.put("SP1", species1.getName());
        final SpeciesCodes speciesCodes = new SpeciesCodes(sCodes);

        final Map<String, IntFunction<SmallLargeAllocationGridsSupplier.SizeGroup>> binToSizeGroupMappings = new HashMap<>();
        binToSizeGroupMappings.put("Piano Tuna", entry -> entry == 0 ? SMALL : LARGE);

        final GlobalBiology globalBiology = new GlobalBiology(species1);
        final HashMap<Species, double[][]> abundance = new HashMap<>();
        abundance.put(species1, new double[][]{{10, 10}, {10, 10}});

        final HashMap<Species, double[][]> fadAbundance = new HashMap<>();
        fadAbundance.put(species1, new double[][]{{2, 1}, {3, 4}});


        final AbundanceAggregator aggregator = new AbundanceAggregator();

        final List<AbundanceLocalBiology> localBiologies;
        localBiologies = new ArrayList<>();
        localBiologies.add(new AbundanceLocalBiology(abundance));

        final List<AbundanceLocalBiology> fadBiologies;
        fadBiologies = new ArrayList<>();
        fadBiologies.add(new AbundanceLocalBiology(fadAbundance));


        final FadAbundanceExcluder fadAbundanceExcluder = new FadAbundanceExcluder();

        final AbundanceLocalBiology excludedAbundance = fadAbundanceExcluder.exclude(
            new AbundanceLocalBiology(abundance),
            new AbundanceLocalBiology(fadAbundance)
        );

        Assertions.assertEquals(excludedAbundance.getAbundance(species1).asMatrix()[0][0], 8, 0);
        Assertions.assertEquals(excludedAbundance.getAbundance(species1).asMatrix()[1][0], 7, 0);
        Assertions.assertEquals(excludedAbundance.getAbundance(species1).asMatrix()[0][1], 9, 0);
        Assertions.assertEquals(excludedAbundance.getAbundance(species1).asMatrix()[1][1], 6, 0);

//        System.out.println("breakpoint");

    }


}
