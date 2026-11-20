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

package uk.ac.ox.poseidon.biology.allocators;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static uk.ac.ox.poseidon.biology.allocators.Factories.fullCarryingCapacityAllocator;
import static uk.ac.ox.poseidon.biology.allocators.Factories.proportionOfCarryingCapacityAllocator;

import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.ac.ox.poseidon.biology.biomass.CarryingCapacityGrid;
import uk.ac.ox.poseidon.core.Scope;
import uk.ac.ox.poseidon.geography.grids.BaseDoubleGrid;
import uk.ac.ox.poseidon.geography.grids.Cell;

final class ProportionOfCarryingCapacityAllocatorTest {

  private static final Cell CELL = new Cell(0, 0);
  private final CarryingCapacityGrid capacityGrid = new CarryingCapacityGrid(
    new BaseDoubleGrid(new double[][] { { 80 }, { 0 } })
  );

  @Test
  void constantProportionScalesEachCellsCarryingCapacity() {
    final ProportionOfCarryingCapacityAllocator allocator =
      proportionOfCarryingCapacityAllocator(s -> capacityGrid, 0.25).get(
        Scope.GLOBAL_SCOPE
      );

    assertThat(allocator.applyAsDouble(CELL)).isEqualTo(20);
    assertThat(allocator.applyAsDouble(new Cell(1, 0))).isZero();
  }

  @Test
  void fullCapacityHelperAllocatesTheEntireCapacity() {
    final ProportionOfCarryingCapacityAllocator allocator =
      fullCarryingCapacityAllocator(s -> capacityGrid).get(Scope.GLOBAL_SCOPE);

    assertThat(allocator.applyAsDouble(CELL)).isEqualTo(80);
  }

  @Test
  void zeroProportionAllocatesNothing() {
    final ProportionOfCarryingCapacityAllocator allocator =
      proportionOfCarryingCapacityAllocator(s -> capacityGrid, 0).get(
        Scope.GLOBAL_SCOPE
      );

    assertThat(allocator.applyAsDouble(CELL)).isZero();
  }

  @Test
  void liveProportionIsReadOnEachAllocation() {
    final AtomicReference<Double> proportion = new AtomicReference<>(0.25);
    final ProportionOfCarryingCapacityAllocator allocator =
      proportionOfCarryingCapacityAllocator(
        s -> capacityGrid,
        s -> context -> proportion.get()
      ).get(Scope.GLOBAL_SCOPE);

    assertThat(allocator.applyAsDouble(CELL)).isEqualTo(20);

    proportion.set(0.5);

    assertThat(allocator.applyAsDouble(CELL)).isEqualTo(40);
  }

  @ParameterizedTest
  @ValueSource(doubles = { -0.1, 1.1 })
  void rejectsProportionsOutsideTheUnitInterval(final double proportion) {
    final ProportionOfCarryingCapacityAllocator allocator =
      proportionOfCarryingCapacityAllocator(s -> capacityGrid, proportion).get(
        Scope.GLOBAL_SCOPE
      );

    assertThatIllegalArgumentException().isThrownBy(() ->
      allocator.applyAsDouble(CELL)
    );
  }
}
