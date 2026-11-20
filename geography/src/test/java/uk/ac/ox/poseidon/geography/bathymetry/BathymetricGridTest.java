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

package uk.ac.ox.poseidon.geography.bathymetry;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.ac.ox.poseidon.geography.Envelope;
import uk.ac.ox.poseidon.geography.grids.Cell;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;

final class BathymetricGridTest {

  private final BathymetricGrid bathymetricGrid = new DefaultBathymetricGrid(
    ModelGrid.create(5, 3, new Envelope(0, 5, 0, 3)).withActiveCells(
      List.of(
        new Cell(0, 0),
        new Cell(1, 0),
        new Cell(1, 1),
        new Cell(2, 1),
        new Cell(2, 2),
        new Cell(3, 0),
        new Cell(4, 2)
      )
    ),
    new double[][] {
      { -9, 4, -3 },
      { 0, -6, 2 },
      { -2, 5, -4 },
      { 3, -8, 0 },
      { -1, 7, -5 }
    }
  );

  @ParameterizedTest
  @CsvSource({ "0, 0, true", "1, 0, false", "0, 1, false" })
  void onlyNegativeElevationsAreWater(
    final int x,
    final int y,
    final boolean expectedWater
  ) {
    final var cell = new Cell(x, y);

    assertThat(bathymetricGrid.isWater(cell)).isEqualTo(expectedWater);
    assertThat(bathymetricGrid.isLand(cell)).isEqualTo(!expectedWater);
  }

  @ParameterizedTest
  @CsvSource({ "0, 0, 9", "1, 0, 0", "0, 1, -4" })
  void depthIsTheNegationOfElevation(
    final int x,
    final int y,
    final double expectedDepth
  ) {
    assertThat(bathymetricGrid.getDepth(new Cell(x, y))).isEqualTo(
      expectedDepth
    );
  }

  @ParameterizedTest
  @CsvSource({ "0, 0, true", "0, 2, false", "1, 0, false", "0, 1, false" })
  void activeWaterRequiresBothWaterAndAnActiveCell(
    final int x,
    final int y,
    final boolean expectedActiveWater
  ) {
    assertThat(bathymetricGrid.isActiveWater(new Cell(x, y))).isEqualTo(
      expectedActiveWater
    );
  }

  @Test
  void waterCellsIncludeInactiveWater() {
    assertThat(bathymetricGrid.getWaterCells()).containsExactlyInAnyOrder(
      new Cell(0, 0),
      new Cell(0, 2),
      new Cell(1, 1),
      new Cell(2, 0),
      new Cell(2, 2),
      new Cell(3, 1),
      new Cell(4, 0),
      new Cell(4, 2)
    );
  }

  @Test
  void landCellsIncludeInactiveLandAndZeroElevation() {
    assertThat(bathymetricGrid.getLandCells()).containsExactlyInAnyOrder(
      new Cell(0, 1),
      new Cell(1, 0),
      new Cell(1, 2),
      new Cell(2, 1),
      new Cell(3, 0),
      new Cell(3, 2),
      new Cell(4, 1)
    );
  }

  @Test
  void activeWaterCellsExcludeInactiveWaterAndActiveLand() {
    assertThat(bathymetricGrid.getActiveWaterCells()).containsExactlyInAnyOrder(
      new Cell(0, 0),
      new Cell(1, 1),
      new Cell(2, 2),
      new Cell(4, 2)
    );
  }

  @Test
  void allCellsIncludeLandAndWaterRegardlessOfActivity() {
    assertThat(bathymetricGrid.getAllCells()).containsExactlyInAnyOrder(
      new Cell(0, 0),
      new Cell(0, 1),
      new Cell(0, 2),
      new Cell(1, 0),
      new Cell(1, 1),
      new Cell(1, 2),
      new Cell(2, 0),
      new Cell(2, 1),
      new Cell(2, 2),
      new Cell(3, 0),
      new Cell(3, 1),
      new Cell(3, 2),
      new Cell(4, 0),
      new Cell(4, 1),
      new Cell(4, 2)
    );
  }

  @Test
  void defaultWaterNeighboursAreWithinOneCell() {
    assertThat(
      bathymetricGrid.getWaterNeighbours(new Cell(2, 1))
    ).containsExactlyInAnyOrder(
      new Cell(1, 1),
      new Cell(2, 0),
      new Cell(2, 2),
      new Cell(3, 1)
    );
  }

  @Test
  void defaultLandNeighboursIncludeDiagonalsAndExcludeTheOrigin() {
    assertThat(
      bathymetricGrid.getLandNeighbours(new Cell(2, 1))
    ).containsExactlyInAnyOrder(
      new Cell(1, 0),
      new Cell(1, 2),
      new Cell(3, 0),
      new Cell(3, 2)
    );
  }

  @Test
  void defaultActiveWaterNeighboursExcludeInactiveWater() {
    assertThat(
      bathymetricGrid.getActiveWaterNeighbours(new Cell(2, 1))
    ).containsExactlyInAnyOrder(new Cell(1, 1), new Cell(2, 2));
  }

  @Test
  void defaultActiveLandNeighboursExcludeInactiveLandAndTheOrigin() {
    assertThat(
      bathymetricGrid.getActiveLandNeighbours(new Cell(2, 1))
    ).containsExactlyInAnyOrder(new Cell(1, 0), new Cell(3, 0));
  }

  @Test
  void waterNeighboursUseTheRequestedSizeAndExcludeTheWaterOrigin() {
    assertThat(
      bathymetricGrid.getWaterNeighbours(new Cell(2, 2), 2)
    ).containsExactlyInAnyOrder(
      new Cell(0, 0),
      new Cell(0, 2),
      new Cell(1, 1),
      new Cell(2, 0),
      new Cell(3, 1),
      new Cell(4, 0),
      new Cell(4, 2)
    );
  }

  @Test
  void landNeighboursUseTheRequestedSize() {
    assertThat(
      bathymetricGrid.getLandNeighbours(new Cell(2, 1), 2)
    ).containsExactlyInAnyOrder(
      new Cell(0, 1),
      new Cell(1, 0),
      new Cell(1, 2),
      new Cell(3, 0),
      new Cell(3, 2),
      new Cell(4, 1)
    );
  }

  @Test
  void activeWaterNeighboursUseTheRequestedSizeAndExcludeTheWaterOrigin() {
    assertThat(
      bathymetricGrid.getActiveWaterNeighbours(new Cell(2, 2), 2)
    ).containsExactlyInAnyOrder(new Cell(0, 0), new Cell(1, 1), new Cell(4, 2));
  }

  @Test
  void activeLandNeighboursUseTheRequestedSize() {
    assertThat(
      bathymetricGrid.getActiveLandNeighbours(new Cell(2, 2), 2)
    ).containsExactlyInAnyOrder(new Cell(1, 0), new Cell(2, 1), new Cell(3, 0));
  }

  @Test
  void aSingleCellHasNoNeighboursOfEitherKind() {
    final BathymetricGrid grid = new DefaultBathymetricGrid(new double[][] {
      { -3 }
    });
    final var cell = new Cell(0, 0);

    assertThat(grid.getWaterNeighbours(cell)).isEmpty();
    assertThat(grid.getLandNeighbours(cell)).isEmpty();
    assertThat(grid.getActiveWaterNeighbours(cell)).isEmpty();
    assertThat(grid.getActiveLandNeighbours(cell)).isEmpty();
  }
}
