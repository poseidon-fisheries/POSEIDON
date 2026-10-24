/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024-2026, University of Oxford.
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

package uk.ac.ox.poseidon.geography.paths.astar;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.badlogic.gdx.ai.pfa.Connection;
import com.badlogic.gdx.utils.Array;
import com.google.common.collect.Streams;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.geography.Coordinate;
import uk.ac.ox.poseidon.geography.bathymetry.BathymetricGrid;
import uk.ac.ox.poseidon.geography.bathymetry.DefaultBathymetricGrid;
import uk.ac.ox.poseidon.geography.distance.DistanceCalculator;
import uk.ac.ox.poseidon.geography.distance.EquirectangularDistanceCalculator;
import uk.ac.ox.poseidon.geography.grids.Cell;
import uk.ac.ox.poseidon.geography.paths.AStarPathFinder;
import uk.ac.ox.poseidon.geography.paths.GridAdaptor;
import uk.ac.ox.poseidon.geography.ports.MutablePortGrid;
import uk.ac.ox.poseidon.geography.ports.Port;

class GridAdaptorTest {

  private final BathymetricGrid bathymetricGrid = new DefaultBathymetricGrid(
    new double[][] { { +1d, -1d, -1d }, { +1d, +1d, -1d }, { -1d, -1d, -1d } }
  );

  private final DistanceCalculator distanceCalculator =
    new EquirectangularDistanceCalculator(bathymetricGrid.getModelGrid());

  private final Coordinate portCoordinate = new Coordinate(0.5, 2.5);

  private final MutablePortGrid portGrid = new MutablePortGrid(
    bathymetricGrid,
    Map.of(mock(Port.class), new Coordinate(0.5, 2.5))
  );

  private final GridAdaptor gridAdaptor = new GridAdaptor(
    bathymetricGrid,
    portGrid,
    distanceCalculator
  );

  @Test
  void getIndex() {
    Map.of(
      new Cell(0, 0),
      0,
      new Cell(1, 0),
      1,
      new Cell(2, 0),
      2,
      new Cell(0, 1),
      3,
      new Cell(1, 1),
      4,
      new Cell(2, 1),
      5,
      new Cell(0, 2),
      6,
      new Cell(1, 2),
      7,
      new Cell(2, 2),
      8
    ).forEach((key, value) ->
      assertThat(gridAdaptor.getIndex(key)).isEqualTo(value)
    );
  }

  @Test
  void getNodeCount() {
    assertThat(gridAdaptor.getNodeCount()).isEqualTo(9);
  }

  @Test
  void getConnections() {
    Map.<Cell, Set<Cell>>of(
      new Cell(0, 0),
      Set.of(new Cell(0, 1)),
      new Cell(1, 0),
      Set.of(),
      new Cell(2, 0),
      Set.of(new Cell(2, 1)),
      new Cell(0, 1),
      Set.of(new Cell(0, 0), new Cell(0, 2), new Cell(1, 2)),
      new Cell(1, 1),
      Set.of(),
      new Cell(2, 1),
      Set.of(new Cell(1, 2), new Cell(2, 0), new Cell(2, 2)),
      new Cell(0, 2),
      Set.of(new Cell(0, 1), new Cell(1, 2)),
      new Cell(1, 2),
      Set.of(new Cell(2, 1), new Cell(0, 1), new Cell(0, 2), new Cell(2, 2)),
      new Cell(2, 2),
      Set.of(new Cell(2, 1), new Cell(1, 2))
    ).forEach((from, toNodes) -> {
      final Array<Connection<Cell>> connections = gridAdaptor.getConnections(
        from
      );
      assertThat(Streams.stream(connections)).allMatch(c ->
        c.getFromNode().equals(from)
      );
      assertThat(
        Streams.stream(connections).map(Connection::getToNode)
      ).containsExactlyInAnyOrderElementsOf(toNodes);
    });
  }

  @Test
  void path() {
    final AStarPathFinder pathFinder = new AStarPathFinder(
      bathymetricGrid,
      portGrid,
      distanceCalculator
    );
    assertThat(pathFinder.getPath(new Cell(0, 0), new Cell(2, 0))).isEqualTo(
      Optional.of(
        List.of(
          new Cell(0, 0),
          new Cell(0, 1),
          new Cell(1, 2),
          new Cell(2, 1),
          new Cell(2, 0)
        )
      )
    );
  }
}
