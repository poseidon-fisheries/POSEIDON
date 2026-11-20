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
import static uk.ac.ox.poseidon.geography.allocators.Factories.supplierAllocator;
import static uk.ac.ox.poseidon.geography.grids.Factories.doubleGridFromAllocator;
import static uk.ac.ox.poseidon.geography.grids.Factories.modelGridWithActiveCells;
import static uk.ac.ox.poseidon.geography.grids.Factories.mutableDoubleGridFromAllocator;
import static uk.ac.ox.poseidon.geography.grids.Factories.normalisedDoubleGridFromAllocator;
import static uk.ac.ox.poseidon.geography.grids.Factories.normalisedMutableDoubleGridFromAllocator;
import static uk.ac.ox.poseidon.geography.grids.Factories.staticGridsFromNetCdf;
import static uk.ac.ox.poseidon.geography.grids.Factories.staticNetCdfGridReader;
import static uk.ac.ox.poseidon.geography.grids.Factories.timeIndexedNetCdfGridReader;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.Scope;
import uk.ac.ox.poseidon.geography.Envelope;

final class FactoriesTest {

  private final ModelGrid modelGrid = ModelGrid.create(
    2,
    2,
    new Envelope(0, 2, 0, 2)
  );

  @Test
  void modelGridWithActiveCellsKeepsOnlyTheGivenCellsActive() {
    final ModelGrid grid = modelGridWithActiveCells(
      ignored -> modelGrid,
      ignored -> List.of(new Cell(0, 0), new Cell(1, 1))
    ).get(GLOBAL_SCOPE);

    assertThat(grid.getActiveCells()).containsExactlyInAnyOrder(
      new Cell(0, 0),
      new Cell(1, 1)
    );
  }

  @Test
  void doubleGridFromAllocatorTakesEachCellValueFromTheAllocator() {
    final DoubleGrid grid = doubleGridFromAllocator(
      ignored -> modelGrid,
      supplierAllocator(2.5)
    ).get(GLOBAL_SCOPE);

    assertThat(grid.getSum()).isEqualTo(10.0);
    assertThat(grid.getValue(new Cell(1, 0))).isEqualTo(2.5);
  }

  @Test
  void mutableDoubleGridFromAllocatorTakesEachCellValueFromTheAllocator() {
    final MutableDoubleGrid grid = mutableDoubleGridFromAllocator(
      ignored -> modelGrid,
      supplierAllocator(2.5)
    ).get(GLOBAL_SCOPE);

    assertThat(grid.getSum()).isEqualTo(10.0);
  }

  @Test
  void normalisedDoubleGridFromAllocatorSumsToTheTotalValue() {
    final DoubleGrid grid = normalisedDoubleGridFromAllocator(
      ignored -> modelGrid,
      supplierAllocator(1.0),
      ignored -> 8
    ).get(GLOBAL_SCOPE);

    assertThat(grid.getSum()).isEqualTo(8.0);
    assertThat(grid.getValue(new Cell(0, 1))).isEqualTo(2.0);
  }

  @Test
  void normalisedMutableDoubleGridFromAllocatorSumsToTheTotalValue() {
    final MutableDoubleGrid grid = normalisedMutableDoubleGridFromAllocator(
      ignored -> modelGrid,
      supplierAllocator(1.0),
      ignored -> 8
    ).get(GLOBAL_SCOPE);

    assertThat(grid.getSum()).isEqualTo(8.0);
    assertThat(grid.getValue(new Cell(0, 1))).isEqualTo(2.0);
  }

  @Test
  void timeIndexedNetCdfGridReaderKeepsEachArgumentInItsPlace() {
    final Factory<Scope, Path> path = ignored -> Path.of("grid.nc");

    final var factory = timeIndexedNetCdfGridReader(path, "time", "lat", "lon");

    assertThat(factory.getNcFilePath()).isSameAs(path);
    assertThat(factory.getTimeDimensionName()).isEqualTo("time");
    assertThat(factory.getLatitudeDimensionName()).isEqualTo("lat");
    assertThat(factory.getLongitudeDimensionName()).isEqualTo("lon");
  }

  @Test
  void staticNetCdfGridReaderKeepsEachArgumentInItsPlace() {
    final Factory<Scope, Path> path = ignored -> Path.of("grid.nc");

    final var factory = staticNetCdfGridReader(path, "lat", "lon");

    assertThat(factory.getNcFilePath()).isSameAs(path);
    assertThat(factory.getLatitudeDimensionName()).isEqualTo("lat");
    assertThat(factory.getLongitudeDimensionName()).isEqualTo("lon");
  }

  @Test
  void staticGridsFromNetCdfKeepsEachArgumentInItsPlace() {
    final Factory<Scope, ModelGrid> grid = ignored -> modelGrid;
    final Factory<Scope, Supplier<StaticNetCdfGridReader>> reader = ignored ->
      () ->
        null;

    final var factory = staticGridsFromNetCdf(grid, reader);

    assertThat(factory.getModelGrid()).isSameAs(grid);
    assertThat(factory.getStaticNetCdfGridReader()).isSameAs(reader);
  }
}
