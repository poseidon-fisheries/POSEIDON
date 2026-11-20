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

package uk.ac.ox.poseidon.geography.allocators;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.ox.poseidon.core.Scope.GLOBAL_SCOPE;
import static uk.ac.ox.poseidon.core.providers.constant.Factories.constant;
import static uk.ac.ox.poseidon.geography.allocators.Factories.filteredAllocator;
import static uk.ac.ox.poseidon.geography.allocators.Factories.supplierAllocator;

import java.util.function.BiFunction;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.geography.grids.Cell;

final class FactoriesTest {

  private final Cell cell = new Cell(1, 2);

  @Test
  void supplierAllocatorFromAConstantGivesThatValueEverywhere() {
    final Allocator allocator = supplierAllocator(2.5).get(GLOBAL_SCOPE);

    assertThat(allocator.applyAsDouble(cell)).isEqualTo(2.5);
    assertThat(allocator.applyAsDouble(new Cell(0, 0))).isEqualTo(2.5);
  }

  @Test
  void supplierAllocatorFromAValueFactoryGivesThatValueEverywhere() {
    final Allocator allocator = supplierAllocator(constant(4.0)).get(
      GLOBAL_SCOPE
    );

    assertThat(allocator.applyAsDouble(cell)).isEqualTo(4.0);
  }

  @Test
  void filteredAllocatorGivesNaNWhereThePredicateFails() {
    final BiFunction<Void, Cell, Boolean> predicate = (ignored, c) ->
      c.getX() == 1;
    final Allocator allocator = filteredAllocator(
      supplierAllocator(3.0),
      ignored -> predicate
    ).get(GLOBAL_SCOPE);

    assertThat(allocator.applyAsDouble(cell)).isEqualTo(3.0);
    assertThat(allocator.applyAsDouble(new Cell(0, 2))).isNaN();
  }
}
