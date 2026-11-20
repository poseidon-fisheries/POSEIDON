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

package uk.ac.ox.poseidon.geography.grids;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.ox.poseidon.core.Scope.GLOBAL_SCOPE;
import static uk.ac.ox.poseidon.geography.grids.Factories.modelGridFromLonLatTable;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.Table;
import uk.ac.ox.poseidon.geography.Coordinate;
import uk.ac.ox.poseidon.geography.Envelope;
import uk.ac.ox.poseidon.geography.utils.LonLatTable;

final class ModelGridFromLonLatTableFactoryTest {

  @Test
  void boundsUseAllCoordinatesAndPaddingExtendsEverySide() {
    final var table = table(
      new double[] { 3, 5.75, 1.25 },
      new double[] { 1.5, 0, -1.5 }
    );

    final var grid = modelGridFromLonLatTable(ignored -> table, 10, 0.25).get(
      GLOBAL_SCOPE
    );

    assertThat(grid.getEnvelope()).isEqualTo(new Envelope(1, 6, -1.75, 1.75));
    assertThat(grid.getGridWidth()).isEqualTo(10);
    assertThat(grid.getGridHeight()).isEqualTo(7);
    assertThat(grid.getCellWidth()).isEqualTo(0.5);
    assertThat(grid.getCellHeight()).isEqualTo(0.5);
    assertThat(grid.getActiveCells()).hasSize(70);
  }

  @Test
  void zeroPaddingPreservesTheCoordinatesExactBounds() {
    final var table = table(new double[] { 1, 5 }, new double[] { 10, 12 });

    final var grid = modelGridFromLonLatTable(ignored -> table, 8, 0).get(
      GLOBAL_SCOPE
    );

    assertThat(grid.getEnvelope()).isEqualTo(new Envelope(1, 5, 10, 12));
    assertThat(grid.getGridWidth()).isEqualTo(8);
    assertThat(grid.getGridHeight()).isEqualTo(4);
  }

  @ParameterizedTest
  @CsvSource({ "2.49, 2", "2.5, 3", "2.51, 3" })
  void heightRoundsToTheNearestCellWithHalvesRoundedUp(
    final double heightInDegrees,
    final int expectedHeight
  ) {
    final var table = table(
      new double[] { 1, 5 },
      new double[] { 0, heightInDegrees }
    );

    final var grid = modelGridFromLonLatTable(ignored -> table, 4, 0).get(
      GLOBAL_SCOPE
    );

    assertThat(grid.getGridWidth()).isEqualTo(4);
    assertThat(grid.getGridHeight()).isEqualTo(expectedHeight);
  }

  @Test
  void aSingleCoordinateWithPaddingProducesACentredSquareGrid() {
    final var table = table(new double[] { 3 }, new double[] { 40 });

    final var grid = modelGridFromLonLatTable(ignored -> table, 3, 0.5).get(
      GLOBAL_SCOPE
    );

    assertThat(grid.getEnvelope()).isEqualTo(
      new Envelope(2.5, 3.5, 39.5, 40.5)
    );
    assertThat(grid.getGridWidth()).isEqualTo(3);
    assertThat(grid.getGridHeight()).isEqualTo(3);
    assertThat(grid.toCoordinate(new Cell(1, 1))).isEqualTo(
      new Coordinate(3, 40)
    );
  }

  private static LonLatTable table(
    final double[] longitudes,
    final double[] latitudes
  ) {
    return new LonLatTable(
      Table.create("coordinates").addColumns(
        DoubleColumn.create("lon", longitudes),
        DoubleColumn.create("lat", latitudes)
      ),
      "lon",
      "lat"
    );
  }
}
