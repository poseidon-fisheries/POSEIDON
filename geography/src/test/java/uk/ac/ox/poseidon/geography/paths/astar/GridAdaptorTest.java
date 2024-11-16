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

package uk.ac.ox.poseidon.geography.paths.astar;

import com.badlogic.gdx.ai.pfa.Connection;
import com.badlogic.gdx.utils.Array;
import com.google.common.collect.Streams;
import com.vividsolutions.jts.geom.Coordinate;
import org.junit.jupiter.api.Test;
import sim.util.Int2D;
import uk.ac.ox.poseidon.geography.bathymetry.BathymetricGrid;
import uk.ac.ox.poseidon.geography.bathymetry.DefaultBathymetricGrid;
import uk.ac.ox.poseidon.geography.distance.Distance;
import uk.ac.ox.poseidon.geography.distance.EquirectangularDistance;
import uk.ac.ox.poseidon.geography.paths.AStarPathFinder;
import uk.ac.ox.poseidon.geography.paths.GridAdaptor;
import uk.ac.ox.poseidon.geography.ports.DefaultPortGrid;
import uk.ac.ox.poseidon.geography.ports.PortGrid;
import uk.ac.ox.poseidon.geography.ports.SimplePort;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GridAdaptorTest {

    private final BathymetricGrid bathymetricGrid =
        new DefaultBathymetricGrid(new double[][]{
            {+1d, -1d, -1d},
            {+1d, +1d, -1d},
            {-1d, -1d, -1d},
        });

    private final Distance distance =
        new EquirectangularDistance(
            bathymetricGrid.getGridExtent()
        );

    private final PortGrid portGrid =
        new DefaultPortGrid(
            bathymetricGrid,
            Map.of(new SimplePort("Test port"), new Coordinate(0.5, 2.5))
        );

    private final GridAdaptor gridAdaptor =
        new GridAdaptor(
            bathymetricGrid,
            portGrid,
            distance
        );

    @Test
    void getIndex() {
        Map.of(
            new Int2D(0, 0), 0,
            new Int2D(1, 0), 1,
            new Int2D(2, 0), 2,
            new Int2D(0, 1), 3,
            new Int2D(1, 1), 4,
            new Int2D(2, 1), 5,
            new Int2D(0, 2), 6,
            new Int2D(1, 2), 7,
            new Int2D(2, 2), 8
        ).forEach((key, value) -> assertEquals(value, gridAdaptor.getIndex(key)));
    }

    @Test
    void getNodeCount() {
        assertEquals(9, gridAdaptor.getNodeCount());
    }

    @Test
    void getConnections() {

        Map.of(
            new Int2D(0, 0), Set.of(
                new Int2D(0, 1)
            ),
            new Int2D(1, 0), Set.of(

            ),
            new Int2D(2, 0), Set.of(
                new Int2D(2, 1)
            ),
            new Int2D(0, 1), Set.of(
                new Int2D(0, 0), new Int2D(0, 2), new Int2D(1, 2)
            ),
            new Int2D(1, 1), Set.of(

            ),
            new Int2D(2, 1), Set.of(
                new Int2D(1, 2), new Int2D(2, 0), new Int2D(2, 2)
            ),
            new Int2D(0, 2), Set.of(
                new Int2D(0, 1), new Int2D(1, 2)
            ),
            new Int2D(1, 2), Set.of(
                new Int2D(2, 1), new Int2D(0, 1), new Int2D(0, 2), new Int2D(2, 2)
            ),
            new Int2D(2, 2), Set.of(
                new Int2D(2, 1), new Int2D(1, 2)
            )
        ).forEach((from, toNodes) -> {
            final Array<Connection<Int2D>> connections = gridAdaptor.getConnections(from);
            assertTrue(
                Streams
                    .stream(connections)
                    .allMatch(c -> c.getFromNode().equals(from))
            );
            assertEquals(
                toNodes,
                Streams
                    .stream(connections)
                    .map(Connection::getToNode)
                    .collect(Collectors.toSet())
            );
        });
    }

    @Test
    void path() {
        final AStarPathFinder pathFinder = new AStarPathFinder(bathymetricGrid, portGrid, distance);
        assertEquals(
            Optional.of(List.of(
                new Int2D(0, 0),
                new Int2D(0, 1),
                new Int2D(1, 2),
                new Int2D(2, 1),
                new Int2D(2, 0)
            )),
            pathFinder.getPath(new Int2D(0, 0), new Int2D(2, 0))
        );
    }
}
