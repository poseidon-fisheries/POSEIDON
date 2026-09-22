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

package uk.ac.ox.poseidon.geography.paths;

import com.google.common.collect.ImmutableList;
import sim.util.Int2D;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;

import static com.google.common.collect.ImmutableList.toImmutableList;

/**
 * A {@link PathFinder} specialized to grid cells ({@link Int2D}). Built via
 * {@link uk.ac.ox.poseidon.geography.paths.Factories}.
 */
public interface GridPathFinder extends PathFinder<Int2D> {

    /** @return whether {@code cell} can be part of a path (water, or a port cell) */
    boolean isNavigable(final Int2D cell);

    /**
     * @param startingCell the cell to search from
     * @return every water cell reachable from {@code startingCell} by an existing path
     */
    default ImmutableList<Int2D> getAccessibleWaterCells(
        final Int2D startingCell
    ) {
        return getModelGrid()
            .getAllCells()
            .filter(this::isWater)
            .filter(cell -> isAccessible(startingCell, cell))
            .collect(toImmutableList());
    }

    /** @return the grid this pathfinder finds paths over */
    ModelGrid getModelGrid();

    /** @return whether {@code cell} is water */
    boolean isWater(final Int2D cell);

    /** @return whether a path exists between {@code start} and {@code end} */
    default boolean isAccessible(
        final Int2D start,
        final Int2D end
    ) {
        return getPath(start, end).isPresent();
    }

    /**
     * @param startingCell      the cell to search from
     * @param neighbourhoodSize how far out to look for neighbours
     * @return {@code startingCell}'s water neighbours, within {@code neighbourhoodSize}, that are
     * reachable from it by an existing path
     */
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
