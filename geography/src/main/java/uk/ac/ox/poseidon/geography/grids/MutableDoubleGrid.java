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

package uk.ac.ox.poseidon.geography.grids;

import sim.field.grid.DoubleGrid2D;
import sim.util.Int2D;

public class MutableDoubleGrid extends DoubleGrid {
    public MutableDoubleGrid(final ModelGrid modelGrid) {
        super(modelGrid);
    }

    public MutableDoubleGrid(
        final ModelGrid modelGrid,
        final double initialValue
    ) {
        super(modelGrid, initialValue);
    }

    public MutableDoubleGrid(
        final ModelGrid modelGrid,
        final double[][] values
    ) {
        super(modelGrid, values);
    }

    public MutableDoubleGrid(
        final ModelGrid modelGrid,
        final DoubleGrid2D grid
    ) {
        super(modelGrid, grid);
    }

    protected void setValue(
        final Int2D cell,
        final double value
    ) {
        this.doubleGrid2D.set(cell.x, cell.y, value);
    }
}
