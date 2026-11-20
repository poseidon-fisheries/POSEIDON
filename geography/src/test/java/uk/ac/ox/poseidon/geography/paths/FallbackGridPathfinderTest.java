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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import sim.field.grid.SparseGrid2D;
import uk.ac.ox.poseidon.geography.Envelope;
import uk.ac.ox.poseidon.geography.bathymetry.BathymetricGrid;
import uk.ac.ox.poseidon.geography.bathymetry.DefaultBathymetricGrid;
import uk.ac.ox.poseidon.geography.distance.CartesianDistanceCalculator;
import uk.ac.ox.poseidon.geography.grids.Cell;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;
import uk.ac.ox.poseidon.geography.ports.MutablePortGrid;

final class FallbackGridPathfinderTest {

  private final GridPathFinder heuristicPathFinder = mock(GridPathFinder.class);
  private final GridPathFinder canonicalPathFinder = mock(GridPathFinder.class);
  private final FallbackGridPathfinder pathFinder = new FallbackGridPathfinder(
    heuristicPathFinder,
    canonicalPathFinder
  );

  @Test
  void usesTheCanonicalModelGrid() {
    final ModelGrid modelGrid = ModelGrid.create(
      3,
      1,
      new Envelope(0, 3, 0, 1)
    );
    when(canonicalPathFinder.getModelGrid()).thenReturn(modelGrid);

    assertThat(pathFinder.getModelGrid()).isSameAs(modelGrid);
    verifyNoInteractions(heuristicPathFinder);
  }

  @Test
  void usesCanonicalWaterClassification() {
    final Cell water = new Cell(0, 0);
    final Cell land = new Cell(1, 0);
    when(canonicalPathFinder.isWater(water)).thenReturn(true);
    when(canonicalPathFinder.isWater(land)).thenReturn(false);

    assertThat(pathFinder.isWater(water)).isTrue();
    assertThat(pathFinder.isWater(land)).isFalse();
    verifyNoInteractions(heuristicPathFinder);
  }

  @Test
  void usesCanonicalNavigability() {
    final Cell navigable = new Cell(0, 0);
    final Cell blocked = new Cell(1, 0);
    when(canonicalPathFinder.isNavigable(navigable)).thenReturn(true);
    when(canonicalPathFinder.isNavigable(blocked)).thenReturn(false);

    assertThat(pathFinder.isNavigable(navigable)).isTrue();
    assertThat(pathFinder.isNavigable(blocked)).isFalse();
    verifyNoInteractions(heuristicPathFinder);
  }

  @Test
  void usesCanonicalReachableWaterCells() {
    final Cell start = new Cell(0, 0);
    when(canonicalPathFinder.getAccessibleWaterCells(start)).thenReturn(
      ImmutableList.of(start, new Cell(1, 0))
    );

    assertThat(
      pathFinder.getAccessibleWaterCells(start)
    ).containsExactlyInAnyOrder(start, new Cell(1, 0));
    verify(canonicalPathFinder).getAccessibleWaterCells(start);
    verifyNoInteractions(heuristicPathFinder);
  }

  @Test
  void usesCanonicalWaterNeighboursWithTheRequestedRadius() {
    final Cell start = new Cell(0, 0);
    when(canonicalPathFinder.getAccessibleWaterNeighbours(start, 2)).thenReturn(
      ImmutableList.of(new Cell(1, 0), new Cell(2, 0))
    );

    assertThat(
      pathFinder.getAccessibleWaterNeighbours(start, 2)
    ).containsExactlyInAnyOrder(new Cell(1, 0), new Cell(2, 0));
    verify(canonicalPathFinder).getAccessibleWaterNeighbours(start, 2);
    verifyNoInteractions(heuristicPathFinder);
  }

  @Test
  void aHeuristicPathAvoidsTheCanonicalSearch() {
    final Cell start = new Cell(0, 0);
    final Cell end = new Cell(2, 0);
    when(heuristicPathFinder.getPath(start, end)).thenReturn(
      Optional.of(ImmutableList.of(start, new Cell(1, 0), end))
    );

    assertThat(pathFinder.getPath(start, end)).hasValueSatisfying(path ->
      assertThat(path).containsExactly(start, new Cell(1, 0), end)
    );
    verifyNoInteractions(canonicalPathFinder);
  }

  @Test
  void aHeuristicMissUsesTheCanonicalPath() {
    final Cell start = new Cell(0, 0);
    final Cell end = new Cell(2, 0);
    when(heuristicPathFinder.getPath(start, end)).thenReturn(Optional.empty());
    when(canonicalPathFinder.getPath(start, end)).thenReturn(
      Optional.of(ImmutableList.of(start, new Cell(1, 1), end))
    );

    assertThat(pathFinder.getPath(start, end)).hasValueSatisfying(path ->
      assertThat(path).containsExactly(start, new Cell(1, 1), end)
    );
    verify(canonicalPathFinder).getPath(start, end);
  }

  @Test
  void returnsEmptyWhenBothSearchesFail() {
    final Cell start = new Cell(0, 0);
    final Cell end = new Cell(2, 0);
    when(heuristicPathFinder.getPath(start, end)).thenReturn(Optional.empty());
    when(canonicalPathFinder.getPath(start, end)).thenReturn(Optional.empty());

    assertThat(pathFinder.getPath(start, end)).isEmpty();
    verify(canonicalPathFinder).getPath(start, end);
  }

  @Test
  void canonicalSearchFindsADetourThroughInactiveWater() {
    final ModelGrid modelGrid = ModelGrid.create(
      3,
      2,
      new Envelope(0, 3, 0, 2)
    ).withActiveCells(List.of(new Cell(0, 0), new Cell(2, 0)));
    final BathymetricGrid bathymetricGrid = new DefaultBathymetricGrid(
      modelGrid,
      new double[][] { { -1, 0 }, { 0, -2 }, { -3, 0 } }
    );
    final var portGrid = new MutablePortGrid(
      bathymetricGrid,
      new SparseGrid2D(3, 2)
    );
    final var heuristic = new BresenhamPathFinder(bathymetricGrid, portGrid);
    final var canonical = new AStarPathFinder(
      bathymetricGrid,
      portGrid,
      new CartesianDistanceCalculator(modelGrid, 1)
    );
    final var fallback = new FallbackGridPathfinder(heuristic, canonical);
    final Cell start = new Cell(0, 0);
    final Cell end = new Cell(2, 0);

    assertThat(heuristic.getPath(start, end)).isEmpty();
    assertThat(fallback.getPath(start, end)).hasValueSatisfying(path ->
      assertThat(path).containsExactly(start, new Cell(1, 1), end)
    );
    assertThat(
      fallback.getAccessibleWaterCells(start)
    ).containsExactlyInAnyOrder(start, new Cell(1, 1), end);
    assertThat(
      fallback.getAccessibleWaterNeighbours(start, 2)
    ).containsExactlyInAnyOrder(new Cell(1, 1), end);
    assertThat(fallback.isNavigable(new Cell(1, 1))).isTrue();
  }
}
