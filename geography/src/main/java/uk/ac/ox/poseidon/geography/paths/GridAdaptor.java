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

import com.badlogic.gdx.ai.pfa.Connection;
import com.badlogic.gdx.ai.pfa.indexed.IndexedGraph;
import com.badlogic.gdx.utils.Array;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import lombok.Data;
import lombok.Getter;
import sim.util.Int2D;
import uk.ac.ox.poseidon.geography.bathymetry.BathymetricGrid;
import uk.ac.ox.poseidon.geography.distance.Distance;
import uk.ac.ox.poseidon.geography.grids.GridExtent;
import uk.ac.ox.poseidon.geography.ports.PortGrid;

import java.util.concurrent.ExecutionException;

public class GridAdaptor implements IndexedGraph<Int2D> {

    private final Interner<Int2D> interner = Interners.newStrongInterner();
    private final BathymetricGrid bathymetricGrid;
    private final PortGrid portGrid;
    private final GridExtent gridExtent;
    @Getter
    private final Distance distance;
    private final Cache<Int2D, Array<Connection<Int2D>>> connectionsCache =
        CacheBuilder.newBuilder().build();

    public GridAdaptor(
        final BathymetricGrid bathymetricGrid,
        final PortGrid portGrid,
        final Distance distance
    ) {
        this.bathymetricGrid = bathymetricGrid;
        this.portGrid = portGrid;
        this.gridExtent = bathymetricGrid.getGridExtent();
        this.distance = distance;
    }

    private boolean isNavigable(final Int2D cell) {
        return bathymetricGrid.isWater(cell) || portGrid.getPortsAt(cell).findAny().isPresent();
    }

    @Override
    public int getIndex(final Int2D cell) {
        return cell.x + cell.y * gridExtent.getGridWidth();
    }

    @Override
    public int getNodeCount() {
        return gridExtent.getGridHeight() * gridExtent.getGridWidth();
    }

    @Override
    public Array<Connection<Int2D>> getConnections(final Int2D cell) {
        try {
            return connectionsCache.get(
                cell,
                () ->
                    !isNavigable(cell) ? new Array<>() : new Array<>(
                        gridExtent
                            .getNeighbours(cell)
                            .stream()
                            .filter(this::isNavigable)
                            .map(neighbour ->
                                new WeightedConnection(
                                    intern(cell),
                                    intern(neighbour),
                                    (float) distance.distanceBetween(cell, neighbour)
                                )
                            )
                            .toArray(WeightedConnection[]::new)
                    )
            );
        } catch (final ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * We need to intern (i.e., map to a canonical instance) all instances of Int2D because the
     * AStarPathFinder does all its comparisons by reference, not equality
     *
     * @param cell any Int2D cell instance
     * @return the canonical instance for that cell
     */
    public Int2D intern(final Int2D cell) {
        return interner.intern(cell);
    }

    @Data
    private static class WeightedConnection implements Connection<Int2D> {
        private final Int2D fromNode;
        private final Int2D toNode;
        private final float cost;
    }

}