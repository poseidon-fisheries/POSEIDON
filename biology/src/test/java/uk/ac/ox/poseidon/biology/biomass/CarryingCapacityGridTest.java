/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024-2025, University of Oxford.
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

package uk.ac.ox.poseidon.biology.biomass;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sim.util.Int2D;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;
import uk.ac.ox.poseidon.geography.grids.ModelGridFactory;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class CarryingCapacityGridTest {

    private CarryingCapacityGrid grid;

    @BeforeEach
    void setUp() {
        final ModelGrid modelGrid =
            new ModelGridFactory(1.0, -1.5, 1.5, -1.5, 1.5)
                .get(mock(Simulation.class));
        grid = new CarryingCapacityGrid(
            modelGrid,
            new double[][]{
                {0, 0, 1},
                {0, 0, 1},
                {0, 0, 1}
            }
        );
    }

    @Test
    void getCarryingCapacity() {
        grid.getModelGrid().getAllCells().forEach(cell ->
            assertEquals(cell.y == 2 ? 1 : 0, grid.getCarryingCapacity(cell))
        );
    }

    @Test
    void getHabitableLocations() {
        assertEquals(
            Set.of(new Int2D(0, 2), new Int2D(1, 2), new Int2D(2, 2)),
            new HashSet<>(grid.getHabitableCells())
        );
    }
}
