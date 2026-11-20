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
import static uk.ac.ox.poseidon.core.Scope.GLOBAL_SCOPE;
import static uk.ac.ox.poseidon.core.aggregators.Factories.meanAggregator;
import static uk.ac.ox.poseidon.geography.bathymetry.Factories.bathymetricGridFromElevationTable;

import org.junit.jupiter.api.Test;
import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.Table;
import uk.ac.ox.poseidon.geography.Coordinate;
import uk.ac.ox.poseidon.geography.grids.Cell;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;
import uk.ac.ox.poseidon.geography.grids.ModelGridFactory;
import uk.ac.ox.poseidon.geography.utils.ElevationTable;

final class BathymetricGridFromElevationTableFactoryTest {

  private final ModelGrid modelGrid = new ModelGridFactory(1, -1, 1, -1, 1).get(
    GLOBAL_SCOPE
  );

  @Test
  void eachCellGetsTheMeanOfTheSamplesFallingInIt() {
    final BathymetricGrid grid = grid(false);

    assertThat(grid.getElevation(new Cell(0, 0))).isEqualTo(-30.0);
    assertThat(grid.getElevation(new Cell(1, 1))).isEqualTo(5.0);
  }

  @Test
  void cellsWithoutSamplesDefaultToZero() {
    final BathymetricGrid grid = grid(false);

    assertThat(grid.getElevation(new Cell(1, 0))).isEqualTo(0.0);
    assertThat(grid.getElevation(new Cell(0, 1))).isEqualTo(0.0);
  }

  @Test
  void invertingNegatesEverySample() {
    final BathymetricGrid grid = grid(true);

    assertThat(grid.getElevation(new Cell(0, 0))).isEqualTo(30.0);
    assertThat(grid.getElevation(new Cell(1, 1))).isEqualTo(-5.0);
  }

  private BathymetricGrid grid(final boolean inverted) {
    final Coordinate first = modelGrid.toCoordinate(new Cell(0, 0));
    final Coordinate second = modelGrid.toCoordinate(new Cell(1, 1));
    final ElevationTable table = new ElevationTable(
      Table.create("elevations").addColumns(
        DoubleColumn.create("lon", new double[] {
          first.lon,
          first.lon,
          second.lon
        }),
        DoubleColumn.create("lat", new double[] {
          first.lat,
          first.lat,
          second.lat
        }),
        DoubleColumn.create("elevation", new double[] { -20, -40, 5 })
      ),
      "lon",
      "lat",
      "elevation"
    );
    return bathymetricGridFromElevationTable(
      ignored -> table,
      ignored -> modelGrid,
      meanAggregator(),
      inverted
    ).get(GLOBAL_SCOPE);
  }
}
