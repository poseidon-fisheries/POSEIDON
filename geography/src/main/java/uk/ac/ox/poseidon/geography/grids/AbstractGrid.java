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

import sim.field.grid.Grid2D;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A {@link Grid} base class wrapping a MASON {@link Grid2D} of type {@code F}, sized to match a
 * {@link ModelGrid}.
 */
public abstract class AbstractGrid<F extends Grid2D> implements Grid {

    /** The underlying MASON field, sized to match {@link #getModelGrid()}. */
    protected final F field;
    private final ModelGrid modelGrid;

    /**
     * @param modelGrid the grid this is defined over
     * @param field     the underlying MASON field; must have the same dimensions as
     *                  {@code modelGrid}
     * @throws IllegalArgumentException if {@code field}'s dimensions don't match
     *                                   {@code modelGrid}'s, or either is non-positive
     */
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

    @Override
    public ModelGrid getModelGrid() {
        return modelGrid;
    }
}
