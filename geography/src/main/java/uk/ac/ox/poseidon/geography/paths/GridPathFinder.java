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

package uk.ac.ox.poseidon.geography.paths;

import com.google.common.collect.ImmutableList;
import sim.util.Int2D;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;

import static com.google.common.collect.ImmutableList.toImmutableList;

public interface GridPathFinder extends PathFinder<Int2D> {

    boolean isNavigable(final Int2D cell);

    default ImmutableList<Int2D> getAccessibleWaterCells(
        final Int2D startingCell
    ) {
        return getModelGrid()
            .getAllCells()
            .filter(this::isWater)
            .filter(cell -> isAccessible(startingCell, cell))
            .collect(toImmutableList());
    }

    ModelGrid getModelGrid();

    boolean isWater(final Int2D cell);

    default boolean isAccessible(
        final Int2D start,
        final Int2D end
    ) {
        return getPath(start, end).isPresent();
    }

    default ImmutableList<Int2D> getAccessibleWaterNeighbours(
        final Int2D startingCell,
        final int neighbourhoodSize
    ) {
        return getModelGrid()
            .getActiveNeighbours(startingCell, neighbourhoodSize)
            .stream()
            .filter(this::isWater)
            .filter(cell -> isAccessible(startingCell, cell))
            .collect(toImmutableList());
    }

}
