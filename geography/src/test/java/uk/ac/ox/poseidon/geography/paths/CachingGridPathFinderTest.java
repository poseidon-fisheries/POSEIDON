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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.ac.ox.poseidon.geography.ports.Factories.port;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.Scope;
import uk.ac.ox.poseidon.geography.Envelope;
import uk.ac.ox.poseidon.geography.bathymetry.DefaultBathymetricGrid;
import uk.ac.ox.poseidon.geography.distance.CartesianDistanceCalculator;
import uk.ac.ox.poseidon.geography.grids.Cell;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;
import uk.ac.ox.poseidon.geography.ports.MutablePortGrid;

final class CachingGridPathFinderTest {

  private final ModelGrid modelGrid = ModelGrid.create(
    6,
    1,
    new Envelope(0, 6, 0, 1)
  ).withActiveCells(List.of(new Cell(2, 0), new Cell(3, 0), new Cell(4, 0)));
  private final DefaultBathymetricGrid bathymetricGrid =
    new DefaultBathymetricGrid(modelGrid, new double[][] {
      { 0 },
      { -1 },
      { -2 },
      { 0 },
      { -3 },
      { 1 }
    });
  private final MutablePortGrid portGrid = new MutablePortGrid(
    bathymetricGrid,
    Map.of(
      port("P", "Port P").get(Scope.GLOBAL_SCOPE),
      modelGrid.toCoordinate(new Cell(0, 0))
    )
  );
  private final GridPathFinder delegate = spy(
    new AStarPathFinder(
      bathymetricGrid,
      portGrid,
      new CartesianDistanceCalculator(modelGrid, 1)
    )
  );
  private final CachingGridPathFinder pathFinder = new CachingGridPathFinder(
    delegate,
    new DefaultPathCache(modelGrid),
    new AccessibleWaterCellsCache()
  );

  @Test
  void exposesTheDelegatesModelGrid() {
    assertThat(pathFinder.getModelGrid()).isSameAs(modelGrid);
  }

  @Test
  void distinguishesWaterFromLandRegardlessOfActivity() {
    assertThat(pathFinder.isWater(new Cell(1, 0))).isTrue();
    assertThat(pathFinder.isWater(new Cell(2, 0))).isTrue();
    assertThat(pathFinder.isWater(new Cell(0, 0))).isFalse();
    assertThat(pathFinder.isWater(new Cell(3, 0))).isFalse();
  }

  @Test
  void portsAndInactiveWaterAreNavigableButOrdinaryLandIsNot() {
    assertThat(pathFinder.isNavigable(new Cell(0, 0))).isTrue();
    assertThat(pathFinder.isNavigable(new Cell(1, 0))).isTrue();
    assertThat(pathFinder.isNavigable(new Cell(3, 0))).isFalse();
  }

  @Test
  void cachesReachableWaterIncludingInactiveCellsAndExcludingLand() {
    final Cell start = new Cell(0, 0);

    assertThat(
      pathFinder.getAccessibleWaterCells(start)
    ).containsExactlyInAnyOrder(new Cell(1, 0), new Cell(2, 0));
    assertThat(
      pathFinder.getAccessibleWaterCells(start)
    ).containsExactlyInAnyOrder(new Cell(1, 0), new Cell(2, 0));
    assertThat(
      pathFinder.getAccessibleWaterCells(new Cell(4, 0))
    ).containsExactly(new Cell(4, 0));
    verify(delegate, times(1)).getAccessibleWaterCells(start);
    verify(delegate, times(1)).getAccessibleWaterCells(new Cell(4, 0));
  }

  @Test
  void cachesWaterNeighboursSeparatelyByOriginAndRadius() {
    final Cell start = new Cell(0, 0);

    assertThat(
      pathFinder.getAccessibleWaterNeighbours(start, 1)
    ).containsExactly(new Cell(1, 0));
    assertThat(
      pathFinder.getAccessibleWaterNeighbours(start, 1)
    ).containsExactly(new Cell(1, 0));
    assertThat(
      pathFinder.getAccessibleWaterNeighbours(start, 2)
    ).containsExactlyInAnyOrder(new Cell(1, 0), new Cell(2, 0));
    assertThat(
      pathFinder.getAccessibleWaterNeighbours(new Cell(2, 0), 1)
    ).containsExactly(new Cell(1, 0));
    verify(delegate, times(1)).getAccessibleWaterNeighbours(start, 1);
    verify(delegate, times(1)).getAccessibleWaterNeighbours(start, 2);
    verify(delegate, times(1)).getAccessibleWaterNeighbours(new Cell(2, 0), 1);
  }

  @Test
  void cachesAnEmptyWaterNeighbourhood() {
    final Cell isolatedCell = new Cell(4, 0);

    assertThat(
      pathFinder.getAccessibleWaterNeighbours(isolatedCell, 5)
    ).isEmpty();
    assertThat(
      pathFinder.getAccessibleWaterNeighbours(isolatedCell, 5)
    ).isEmpty();
    verify(delegate, times(1)).getAccessibleWaterNeighbours(isolatedCell, 5);
  }

  @Test
  void cachesPathsThroughInactiveWaterWithoutReversingQueries() {
    final Cell start = new Cell(0, 0);
    final Cell end = new Cell(2, 0);

    assertThat(pathFinder.getPath(start, end)).hasValueSatisfying(path ->
      assertThat(path).containsExactly(start, new Cell(1, 0), end)
    );
    assertThat(pathFinder.getPath(start, end)).hasValueSatisfying(path ->
      assertThat(path).containsExactly(start, new Cell(1, 0), end)
    );
    assertThat(pathFinder.getPath(end, start)).hasValueSatisfying(path ->
      assertThat(path).containsExactly(end, new Cell(1, 0), start)
    );
    verify(delegate, times(1)).getPath(start, end);
    verify(delegate, times(1)).getPath(end, start);
  }

  @Test
  void cachesTheAbsenceOfAPath() {
    final Cell start = new Cell(1, 0);
    final Cell end = new Cell(4, 0);

    assertThat(pathFinder.getPath(start, end)).isEmpty();
    assertThat(pathFinder.getPath(start, end)).isEmpty();
    verify(delegate, times(1)).getPath(start, end);
  }
}
