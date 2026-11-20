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

package uk.ac.ox.poseidon.geography.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.ox.poseidon.core.Scope.GLOBAL_SCOPE;
import static uk.ac.ox.poseidon.geography.utils.Factories.elevationTable;
import static uk.ac.ox.poseidon.geography.utils.Factories.lonLatTable;

import org.junit.jupiter.api.Test;
import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.Table;
import uk.ac.ox.poseidon.geography.Coordinate;

final class FactoriesTest {

  private final Table table = Table.create("points").addColumns(
    DoubleColumn.create("x", new double[] { 1, 2 }),
    DoubleColumn.create("y", new double[] { 10, 20 }),
    DoubleColumn.create("z", new double[] { -5, 7 })
  );

  @Test
  void lonLatTableReadsCoordinatesFromTheNamedColumns() {
    final LonLatTable lonLat = lonLatTable(ignored -> table, "x", "y").get(
      GLOBAL_SCOPE
    );

    assertThat(lonLat.coordinateStream()).containsExactly(
      new Coordinate(1, 10),
      new Coordinate(2, 20)
    );
  }

  @Test
  void elevationTableReadsCoordinatesAndElevationsFromTheNamedColumns() {
    final ElevationTable elevations = elevationTable(
      ignored -> table,
      "x",
      "y",
      "z"
    ).get(GLOBAL_SCOPE);

    assertThat(elevations.entryStream()).containsExactly(
      new ElevationTable.Entry(new Coordinate(1, 10), -5),
      new ElevationTable.Entry(new Coordinate(2, 20), 7)
    );
  }
}
