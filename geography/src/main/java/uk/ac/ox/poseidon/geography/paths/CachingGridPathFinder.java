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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import sim.util.Int2D;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;

import java.util.Map.Entry;

import static java.util.Map.entry;

public class CachingGridPathFinder extends CachingPathFinder<Int2D> implements GridPathFinder {

    private final GridPathFinder pathFinder;
    private final Interner<ImmutableList<Int2D>> cellListInterner = Interners.newStrongInterner();
    private final LoadingCache<Int2D, ImmutableList<Int2D>> accessibleCells =
        CacheBuilder.newBuilder().build(CacheLoader.from(this::computeAccessibleCells));
    private final LoadingCache<Entry<Int2D, Integer>, ImmutableList<Int2D>> accessibleNeighbours =
        CacheBuilder.newBuilder().build(CacheLoader.from(this::computeAccessibleNeighbours));

    CachingGridPathFinder(
        final GridPathFinder pathFinder,
        final PathCache<Int2D> cache
    ) {
        super(pathFinder, cache);
        this.pathFinder = pathFinder;
    }

    private ImmutableList<Int2D> computeAccessibleCells(final Int2D startingCell) {
        return pathFinder.getAccessibleWaterCells(startingCell);
    }

    @Override
    public ImmutableList<Int2D> getAccessibleWaterCells(final Int2D startingCell) {
        return cellListInterner.intern(accessibleCells.getUnchecked(startingCell));
    }

    private ImmutableList<Int2D> computeAccessibleNeighbours(
        final Entry<Int2D, Integer> entry
    ) {
        return pathFinder.getAccessibleWaterNeighbours(entry.getKey(), entry.getValue());
    }

    @Override
    public ImmutableList<Int2D> getAccessibleWaterNeighbours(
        final Int2D startingCell,
        final int neighbourhoodSize
    ) {
        return accessibleNeighbours.getUnchecked(entry(startingCell, neighbourhoodSize));
    }

    @Override
    public ModelGrid getModelGrid() {
        return pathFinder.getModelGrid();
    }

    @Override
    public boolean isWater(final Int2D cell) {
        return pathFinder.isWater(cell);
    }

    @Override
    public boolean isNavigable(final Int2D cell) {
        return pathFinder.isNavigable(cell);
    }

}
