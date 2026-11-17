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

package uk.ac.ox.poseidon.core.providers;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.ox.poseidon.core.Scope.GLOBAL_SCOPE;
import static uk.ac.ox.poseidon.core.providers.Factories.shiftedInt;
import static uk.ac.ox.poseidon.core.providers.constant.Factories.constant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ShiftedIntTest {

  @ParameterizedTest
  @CsvSource({ "5, 3, 8", "5, -3, 2", "0, 0, 0", "-4, -6, -10" })
  void appliesTheShiftToTheDelegateValue(
    final int value,
    final int shift,
    final int expected
  ) {
    assertThat(
      shiftedInt(constant(value), shift).get(GLOBAL_SCOPE).apply(null)
    ).isEqualTo(expected);
  }

  @Test
  void appliesTheShiftOnEveryCall() {
    final var shifted = shiftedInt(constant(10), 1).get(GLOBAL_SCOPE);

    assertThat(shifted.apply(null)).isEqualTo(11);
    assertThat(shifted.apply(null)).isEqualTo(11);
  }
}
