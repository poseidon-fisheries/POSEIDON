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
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import org.junit.jupiter.api.Test;

class LessThanTest {

  @Test
  void testValueLessThanThreshold() {
    final double threshold = 10.0;
    final LessThan<Object, Double> lessThan = new LessThan<>(
      context -> threshold
    );

    assertThat(lessThan.apply(null, 5.0)).isTrue();
  }

  @Test
  void testValueEqualsThreshold() {
    final double threshold = 10.0;
    final LessThan<Object, Double> lessThan = new LessThan<>(
      context -> threshold
    );

    assertThat(lessThan.apply(null, 10.0)).isFalse();
  }

  @Test
  void testValueAboveThreshold() {
    final double threshold = 10.0;
    final LessThan<Object, Double> lessThan = new LessThan<>(
      context -> threshold
    );

    assertThat(lessThan.apply(null, 15.0)).isFalse();
  }

  @Test
  void testNegativeValueLessThanNegativeThreshold() {
    final double threshold = -5.0;
    final LessThan<Object, Double> lessThan = new LessThan<>(
      context -> threshold
    );

    assertThat(lessThan.apply(null, -10.0)).isTrue();
  }

  @Test
  void testNegativeValueEqualsNegativeThreshold() {
    final double threshold = -5.0;
    final LessThan<Object, Double> lessThan = new LessThan<>(
      context -> threshold
    );

    assertThat(lessThan.apply(null, -5.0)).isFalse();
  }

  @Test
  void testNegativeValueGreaterThanNegativeThreshold() {
    final double threshold = -10.0;
    final LessThan<Object, Double> lessThan = new LessThan<>(
      context -> threshold
    );

    assertThat(lessThan.apply(null, -5.0)).isFalse();
  }

  @Test
  void testWithZeroValue() {
    final double threshold = 0.0;
    final LessThan<Object, Double> lessThan = new LessThan<>(
      context -> threshold
    );

    assertThat(lessThan.apply(null, -1.0)).isTrue();
  }

  @Test
  void testValueIsNull() {
    final double threshold = 10.0;
    final LessThan<Object, Double> lessThan = new LessThan<>(
      context -> threshold
    );

    assertThatNullPointerException().isThrownBy(() ->
      lessThan.apply(null, null)
    );
  }
}
