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

package uk.ac.ox.poseidon.geography.grids;

import lombok.Getter;
import sim.field.grid.Grid2D;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@Getter
public abstract class AbstractGrid<F extends Grid2D> implements Grid<F> {

    private final GridExtent gridExtent;
    private final F field;

    protected AbstractGrid(
        final GridExtent gridExtent,
        final F field
    ) {
        checkNotNull(gridExtent);
        checkNotNull(field);
        checkArgument(field.getWidth() > 0);
        checkArgument(field.getHeight() > 0);
        checkArgument(gridExtent.getGridWidth() == field.getWidth());
        checkArgument(gridExtent.getGridHeight() == field.getHeight());
        this.gridExtent = gridExtent;
        this.field = field;
    }
}