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

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import sim.field.grid.SparseGrid2D;
import uk.ac.ox.poseidon.geography.Envelope;
import uk.ac.ox.poseidon.geography.bathymetry.BathymetricGrid;
import uk.ac.ox.poseidon.geography.bathymetry.DefaultBathymetricGrid;
import uk.ac.ox.poseidon.geography.distance.CartesianDistanceCalculator;
import uk.ac.ox.poseidon.geography.grids.Cell;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;
import uk.ac.ox.poseidon.geography.ports.MutablePortGrid;

final class GridPathFinderTest {

  @ParameterizedTest
  @ValueSource(booleans = { false, true })
  void pathsCanCrossInactiveWater(final boolean useAStar) {
    final var modelGrid = ModelGrid.create(
      3,
      1,
      new Envelope(0, 3, 0, 1)
    ).withActiveCells(List.of(new Cell(0, 0), new Cell(2, 0)));
    final BathymetricGrid bathymetricGrid = new DefaultBathymetricGrid(
      modelGrid,
      new double[][] { { -1 }, { -2 }, { -3 } }
    );
    final var pathFinder = pathFinder(bathymetricGrid, useAStar);

    assertThat(
      pathFinder.getPath(new Cell(0, 0), new Cell(2, 0))
    ).hasValueSatisfying(path ->
      assertThat(path).containsExactly(
        new Cell(0, 0),
        new Cell(1, 0),
        new Cell(2, 0)
      )
    );
  }

  @Test
  void globalWaterCellsIncludeInactiveWaterButExcludeLandAndDisconnectedWater() {
    final var pathFinder = separatedWater();

    assertThat(
      pathFinder.getAccessibleWaterCells(new Cell(0, 0))
    ).containsExactlyInAnyOrder(new Cell(0, 0), new Cell(1, 0));
  }

  @Test
  void waterNeighboursIncludeInactiveWaterButExcludeLandAndDisconnectedWater() {
    final var pathFinder = separatedWater();

    assertThat(
      pathFinder.getAccessibleWaterNeighbours(new Cell(0, 0), 4)
    ).containsExactly(new Cell(1, 0));
  }

  @Test
  void waterNeighboursRespectTheRequestedRadius() {
    final var bathymetricGrid = new DefaultBathymetricGrid(new double[][] {
      { -1 },
      { -2 },
      { -3 },
      { -4 }
    });
    final var pathFinder = pathFinder(bathymetricGrid, true);

    assertThat(
      pathFinder.getAccessibleWaterNeighbours(new Cell(0, 0), 1)
    ).containsExactly(new Cell(1, 0));
    assertThat(
      pathFinder.getAccessibleWaterNeighbours(new Cell(0, 0), 2)
    ).containsExactlyInAnyOrder(new Cell(1, 0), new Cell(2, 0));
  }

  @Test
  void accessibilityRequiresAnExistingPath() {
    final var pathFinder = separatedWater();

    assertThat(
      pathFinder.isAccessible(new Cell(0, 0), new Cell(1, 0))
    ).isTrue();
    assertThat(
      pathFinder.isAccessible(new Cell(0, 0), new Cell(3, 0))
    ).isFalse();
  }

  @Test
  void anIsolatedWaterCellHasNoWaterNeighbours() {
    final var bathymetricGrid = new DefaultBathymetricGrid(new double[][] {
      { -1 }
    });
    final var pathFinder = pathFinder(bathymetricGrid, true);

    assertThat(
      pathFinder.getAccessibleWaterNeighbours(new Cell(0, 0), 1)
    ).isEmpty();
    assertThat(
      pathFinder.getAccessibleWaterCells(new Cell(0, 0))
    ).containsExactly(new Cell(0, 0));
  }

  private static GridPathFinder separatedWater() {
    final var modelGrid = ModelGrid.create(
      4,
      1,
      new Envelope(0, 4, 0, 1)
    ).withActiveCells(List.of(new Cell(0, 0), new Cell(2, 0), new Cell(3, 0)));
    final BathymetricGrid bathymetricGrid = new DefaultBathymetricGrid(
      modelGrid,
      new double[][] { { -1 }, { -2 }, { 0 }, { -3 } }
    );
    return pathFinder(bathymetricGrid, true);
  }

  private static GridPathFinder pathFinder(
    final BathymetricGrid bathymetricGrid,
    final boolean useAStar
  ) {
    final var modelGrid = bathymetricGrid.getModelGrid();
    final var portGrid = new MutablePortGrid(
      bathymetricGrid,
      new SparseGrid2D(modelGrid.getGridWidth(), modelGrid.getGridHeight())
    );
    return useAStar
      ? new AStarPathFinder(
          bathymetricGrid,
          portGrid,
          new CartesianDistanceCalculator(modelGrid, 1)
        )
      : new BresenhamPathFinder(bathymetricGrid, portGrid);
  }
}
