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
import sim.field.grid.DoubleGrid2D;
import uk.ac.ox.oxfish.biology.tuna.SmallLargeAllocationGridsSupplier.Key;
import uk.ac.ox.oxfish.geography.NauticalMap;

import java.nio.file.Paths;
import java.util.Map;

import static uk.ac.ox.oxfish.biology.tuna.SmallLargeAllocationGridsSupplier.SizeGroup.LARGE;
import static uk.ac.ox.oxfish.biology.tuna.SmallLargeAllocationGridsSupplier.SizeGroup.SMALL;
import static uk.ac.ox.oxfish.geography.TestUtilities.makeMap;

public class AllocationGridsTest {

    @Test
    public void AllocationGridsTester() {

        final NauticalMap nauticalMap = makeMap(3, 3);

        final AllocationGrids<Key> allocationGrids =
            new SmallLargeAllocationGridsSupplier(
                Paths.get("inputs", "tests", "mock_grids.csv"),
                nauticalMap.getMapExtent(),
                365
            ).get();

        Assertions.assertEquals(1, allocationGrids.size());

        final Map<Key, DoubleGrid2D> grid = allocationGrids.getGrids().get(0);
        final DoubleGrid2D gridLARGE = grid.get(new Key("SP1", LARGE));
        final DoubleGrid2D gridSMALL = grid.get(new Key("SP1", SMALL));

        Assertions.assertEquals(0.117031221, gridLARGE.get(0, 0), .01);
        Assertions.assertEquals(0.099199173, gridSMALL.get(0, 0), .01);
        Assertions.assertEquals(0.137690519, gridSMALL.get(2, 0), .01);

    }

}
