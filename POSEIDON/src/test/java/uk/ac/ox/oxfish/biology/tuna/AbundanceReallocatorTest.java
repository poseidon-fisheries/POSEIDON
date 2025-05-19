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

import com.google.common.collect.ImmutableSortedMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import sim.field.grid.DoubleGrid2D;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.biology.complicated.Meristics;
import uk.ac.ox.oxfish.biology.tuna.SmallLargeAllocationGridsSupplier.Key;
import uk.ac.ox.oxfish.biology.tuna.SmallLargeAllocationGridsSupplier.SizeGroup;
import uk.ac.ox.oxfish.geography.NauticalMap;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.ac.ox.oxfish.biology.tuna.SmallLargeAllocationGridsSupplier.SizeGroup.LARGE;
import static uk.ac.ox.oxfish.biology.tuna.SmallLargeAllocationGridsSupplier.SizeGroup.SMALL;
import static uk.ac.ox.oxfish.geography.TestUtilities.makeMap;

public class AbundanceReallocatorTest {

    @Test
    public void AbundanceReallocatorTester() {

        final Meristics meristics = mock(Meristics.class);
        when(meristics.getNumberOfSubdivisions()).thenReturn(2);
        when(meristics.getNumberOfBins()).thenReturn(2);

        final Species species1 = new Species("Piano Tuna", "SP1", meristics, false);

        final BiFunction<Species, Integer, SizeGroup> binToSizeGroup =
            (species, bin) -> bin == 0 ? SMALL : LARGE;

        final GlobalBiology globalBiology = new GlobalBiology(species1);
        final HashMap<Species, double[][]> abundance = new HashMap<>();
        abundance.put(species1, new double[][]{{10, 10}, {10, 10}});

        final NauticalMap nauticalMap = makeMap(3, 3);
        nauticalMap.getAllSeaTilesAsList().forEach(seaTile ->
            seaTile.setBiology(new AbundanceLocalBiology(abundance)
            )
        );

        final AllocationGrids<Key> allocationGrids = new SmallLargeAllocationGridsSupplier(
            Paths.get("inputs", "tests", "mock_grids.csv"),
            nauticalMap.getMapExtent(),
            365
        ).get();

        final AbundanceReallocator reallocator = new AbundanceReallocator(
            allocationGrids,
            binToSizeGroup
        );

        final ImmutableSortedMap<Integer, Map<Key, DoubleGrid2D>> grids = allocationGrids.getGrids();
        final AbundanceLocalBiology aggregatedBiology = new AbundanceLocalBiology(abundance);

        reallocator.reallocate(grids.get(0), globalBiology, nauticalMap.getAllSeaTilesAsList(), aggregatedBiology);

        Assertions.assertEquals(
            0.991991733,
            nauticalMap.getAllSeaTilesAsList().get(0).getAbundance(species1).asMatrix()[0][0],
            .0000001
        );

        Assertions.assertEquals(
            1.13292204,
            nauticalMap.getAllSeaTilesAsList().get(4).getAbundance(species1).asMatrix()[0][1],
            .0000001
        );

    }

}
