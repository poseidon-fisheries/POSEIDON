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

import lombok.Getter;
import sim.field.grid.Grid2D;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@Getter
public abstract class AbstractGrid<F extends Grid2D> implements Grid<F> {

    protected final F field;
    private final ModelGrid modelGrid;

    protected AbstractGrid(
        final ModelGrid modelGrid,
        final F field
    ) {
        checkNotNull(modelGrid);
        checkNotNull(field);
        checkArgument(field.getWidth() > 0);
        checkArgument(field.getHeight() > 0);
        checkArgument(modelGrid.getGridWidth() == field.getWidth());
        checkArgument(modelGrid.getGridHeight() == field.getHeight());
        this.modelGrid = modelGrid;
        this.field = field;
    }
}
