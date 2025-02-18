/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024 CoHESyS Lab cohesys.lab@gmail.com
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
 *
 */

package uk.ac.ox.poseidon.biology.biomass;

import lombok.Getter;
import sim.field.grid.DoubleGrid2D;
import sim.util.Int2D;
import uk.ac.ox.poseidon.geography.grids.DoubleGrid;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;

import java.util.List;

public class CarryingCapacityGrid extends DoubleGrid {

    @Getter(lazy = true)
    private final List<Int2D> habitableCells =
        getModelGrid()
            .getAllCells()
            .stream()
            .filter(cell -> getCarryingCapacity(cell) > 0)
            .toList();

    public CarryingCapacityGrid(final ModelGrid modelGrid) {
        super(modelGrid);
    }

    public CarryingCapacityGrid(
        final ModelGrid modelGrid,
        final double initialValue
    ) {
        super(modelGrid, initialValue);
    }

    public CarryingCapacityGrid(
        final ModelGrid modelGrid,
        final double[][] values
    ) {
        super(modelGrid, values);
    }

    public CarryingCapacityGrid(
        final ModelGrid modelGrid,
        final DoubleGrid2D grid
    ) {
        super(modelGrid, grid);
    }

    public double getCarryingCapacity(final Int2D cell) {
        return getDouble(cell);
    }
}
