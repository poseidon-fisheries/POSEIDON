/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024-2026, University of Oxford.
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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.Scope;
import uk.ac.ox.poseidon.geography.grids.BaseDoubleGrid;
import uk.ac.ox.poseidon.geography.grids.Cell;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;
import uk.ac.ox.poseidon.geography.grids.ModelGridFactory;

class CarryingCapacityGridTest {

  private CarryingCapacityGrid grid;

  @BeforeEach
  void setUp() {
    final ModelGrid modelGrid = new ModelGridFactory(
      1.0,
      -1.5,
      1.5,
      -1.5,
      1.5
    ).get(Scope.GLOBAL_SCOPE);
    grid = new CarryingCapacityGrid(
      new BaseDoubleGrid(modelGrid, new double[][] {
        { 0, 0, 1 },
        { 0, 0, 1 },
        { 0, 0, 1 }
      })
    );
  }

  @Test
  void getCarryingCapacity() {
    grid
      .getModelGrid()
      .getAllCells()
      .forEach(cell ->
        assertEquals(cell.y() == 2 ? 1 : 0, grid.getCarryingCapacity(cell))
      );
  }

  @Test
  void getHabitableCells() {
    assertEquals(
      Set.of(new Cell(0, 2), new Cell(1, 2), new Cell(2, 2)),
      new HashSet<>(grid.getHabitableCells())
    );
  }
}
