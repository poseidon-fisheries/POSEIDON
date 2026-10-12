/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2026, University of Oxford.
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

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import sim.util.Int2D;

import java.util.Map.Entry;

import static java.util.Map.entry;

/**
 * Remembers the accessible water cells from a starting cell, and the accessible water neighbours
 * of a cell within a neighbourhood size. Both depend only on the grids a {@link GridPathFinder}
 * navigates, so one cache can serve every pathfinder built from the same grids, including those
 * of simulations running in parallel. Each result is computed once, by the pathfinder that first
 * asks for it; a concurrent request for the same result waits for it rather than computing it
 * again. Equal lists of accessible water cells (from cells of the same connected area) share one
 * instance. Built via {@link Factories#accessibleWaterCellsCache(uk.ac.ox.poseidon.core.Factory,
 * uk.ac.ox.poseidon.core.Factory)}.
 */
public class AccessibleWaterCellsCache {

    private final Interner<ImmutableList<Int2D>> cellListInterner = Interners.newStrongInterner();
    private final Cache<Int2D, ImmutableList<Int2D>> accessibleCells =
        Caffeine.newBuilder().build();
    private final Cache<Entry<Int2D, Integer>, ImmutableList<Int2D>> accessibleNeighbours =
        Caffeine.newBuilder().build();

    ImmutableList<Int2D> getAccessibleWaterCells(
        final Int2D startingCell,
        final GridPathFinder pathFinder
    ) {
        return accessibleCells.get(
            startingCell,
            cell -> cellListInterner.intern(pathFinder.getAccessibleWaterCells(cell))
        );
    }

    ImmutableList<Int2D> getAccessibleWaterNeighbours(
        final Int2D startingCell,
        final int neighbourhoodSize,
        final GridPathFinder pathFinder
    ) {
        return accessibleNeighbours.get(
            entry(startingCell, neighbourhoodSize),
            key -> pathFinder.getAccessibleWaterNeighbours(key.getKey(), key.getValue())
        );
    }
}
