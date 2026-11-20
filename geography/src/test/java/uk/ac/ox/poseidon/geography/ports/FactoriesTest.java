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

package uk.ac.ox.poseidon.geography.ports;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.ox.poseidon.core.Scope.GLOBAL_SCOPE;
import static uk.ac.ox.poseidon.geography.ports.Factories.portGrid;
import static uk.ac.ox.poseidon.geography.ports.Factories.portsFromTable;
import static uk.ac.ox.poseidon.geography.ports.Factories.randomCellsPortGrid;

import java.util.List;
import org.junit.jupiter.api.Test;
import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.Scope;
import uk.ac.ox.poseidon.core.SimulationScope;
import uk.ac.ox.poseidon.core.utils.Pair;
import uk.ac.ox.poseidon.geography.Coordinate;
import uk.ac.ox.poseidon.geography.bathymetry.BathymetricGrid;
import uk.ac.ox.poseidon.geography.distance.DistanceCalculator;

final class FactoriesTest {

  @Test
  void portsFromTableReadsEachPortFromItsNamedColumns() {
    final Table table = Table.create("ports").addColumns(
      StringColumn.create("code", "A", "B"),
      StringColumn.create("name", "Alpha", "Bravo"),
      DoubleColumn.create("lon", 1, 2),
      DoubleColumn.create("lat", 10, 20)
    );

    final List<Pair<Port, Coordinate>> ports = portsFromTable(
      ignored -> table,
      "code",
      "name",
      "lon",
      "lat"
    ).get(GLOBAL_SCOPE);

    assertThat(ports).containsExactly(
      Pair.of(new Port("A", "Alpha"), new Coordinate(1, 10)),
      Pair.of(new Port("B", "Bravo"), new Coordinate(2, 20))
    );
  }

  @Test
  void portGridKeepsEachArgumentInItsPlace() {
    final Factory<Scope, List<Pair<Port, Coordinate>>> ports = ignored ->
      List.of();
    final Factory<Scope, BathymetricGrid> grid = ignored -> null;
    final Factory<Scope, DistanceCalculator> calculator = ignored -> null;

    final var factory = portGrid(ports, grid, calculator);

    assertThat(factory.getPorts()).isSameAs(ports);
    assertThat(factory.getBathymetricGrid()).isSameAs(grid);
    assertThat(factory.getDistanceCalculator()).isSameAs(calculator);
  }

  @Test
  void randomCellsPortGridKeepsEachArgumentInItsPlace() {
    final Factory<SimulationScope, BathymetricGrid> grid = ignored -> null;

    final var factory = randomCellsPortGrid(grid, ignored -> null, 4, 3);

    assertThat(factory.getBathymetricGrid()).isSameAs(grid);
    assertThat(factory.getNumberOfPorts()).isEqualTo(4);
    assertThat(factory.getMinimumAdjacentWaterTiles()).isEqualTo(3);
    assertThat(factory.getRng()).isNotNull();
  }
}
