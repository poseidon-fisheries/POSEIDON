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

package uk.ac.ox.poseidon.core.predicates.comparable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import org.junit.jupiter.api.Test;

class GreaterThanTest {

  @Test
  void testValueAboveThresholdShouldReturnTrue() {
    final double threshold = 10.0;
    final GreaterThan<Object, Double> greaterThan = new GreaterThan<>(
      context -> threshold
    );

    assertThat(greaterThan.apply(null, 15.0)).isTrue();
  }

  @Test
  void testValueBelowThresholdShouldReturnFalse() {
    final double threshold = 10.0;
    final GreaterThan<Object, Double> greaterThan = new GreaterThan<>(
      context -> threshold
    );

    assertThat(greaterThan.apply(null, 5.0)).isFalse();
  }

  @Test
  void testValueEqualToThresholdShouldReturnFalse() {
    final double threshold = 10.0;
    final GreaterThan<Object, Double> greaterThan = new GreaterThan<>(
      context -> threshold
    );

    assertThat(greaterThan.apply(null, 10.0)).isFalse();
  }

  @Test
  void testValueIsNotNull() {
    final double threshold = 10.0;
    final GreaterThan<Object, Double> greaterThan = new GreaterThan<>(
      context -> threshold
    );

    assertThatCode(() ->
      greaterThan.apply(null, 12.0)
    ).doesNotThrowAnyException();
  }

  @Test
  void testValueIsNullShouldThrowException() {
    final double threshold = 10.0;
    final GreaterThan<Object, Double> greaterThan = new GreaterThan<>(
      context -> threshold
    );

    assertThatNullPointerException().isThrownBy(() ->
      greaterThan.apply(null, null)
    );
  }
}
