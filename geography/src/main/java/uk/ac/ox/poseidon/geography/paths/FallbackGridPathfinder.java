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

/**
 * A {@link GridPathFinder} counterpart of {@link FallbackPathfinder}: {@link #getPath} is
 * inherited from {@link FallbackPathfinder}, while the other {@link GridPathFinder} methods
 * delegate straight to the canonical (complete) pathfinder, since it's the one with authoritative
 * knowledge of the grid. Not built directly — see {@link DefaultPathFinderFactory}/
 * {@link uk.ac.ox.poseidon.geography.paths.Factories}.
 */
public class FallbackGridPathfinder extends FallbackPathfinder<Int2D> implements GridPathFinder {

    private final GridPathFinder canonicalPathFinder;

    /**
     * @param heuristicPathFinder  the fast pathfinder tried first
     * @param canonicalPathFinder  the complete pathfinder tried on a heuristic miss, and consulted
     *                             for every other {@link GridPathFinder} method
     */
    public FallbackGridPathfinder(
        final GridPathFinder heuristicPathFinder,
        final GridPathFinder canonicalPathFinder
    ) {
        super(heuristicPathFinder, canonicalPathFinder);
        this.canonicalPathFinder = canonicalPathFinder;
    }

    @Override
    public ModelGrid getModelGrid() {
        return canonicalPathFinder.getModelGrid();
    }

    @Override
    public boolean isWater(final Int2D cell) {
        return canonicalPathFinder.isWater(cell);
    }

    @Override
    public boolean isNavigable(final Int2D cell) {
        return canonicalPathFinder.isNavigable(cell);
    }

    @Override
    public ImmutableList<Int2D> getAccessibleWaterCells(final Int2D startingCell) {
        return canonicalPathFinder.getAccessibleWaterCells(startingCell);
    }

    @Override
    public ImmutableList<Int2D> getAccessibleWaterNeighbours(
        final Int2D startingCell,
        final int neighbourhoodSize
    ) {
        return canonicalPathFinder.getAccessibleWaterNeighbours(startingCell, neighbourhoodSize);
    }
}
