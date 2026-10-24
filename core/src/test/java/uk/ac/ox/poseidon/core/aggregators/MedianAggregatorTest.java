/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

package uk.ac.ox.poseidon.core.aggregators;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.OptionalDouble;
import java.util.stream.DoubleStream;
import org.junit.jupiter.api.Test;

class MedianAggregatorTest {

  /**
   * This class tests the `apply` method in the `Median` class. The `apply`
   * method calculates the median of a double array or a DoubleStream.
   */

  private final MedianAggregator median = new MedianAggregator();

  @Test
  void testApplyWithEmptyArray() {
    final double[] numbers = new double[] {};

    final OptionalDouble result = median.apply(numbers);

    assertThat(result).isEmpty();
  }

  @Test
  void testApplyWithSingleElement() {
    final double[] numbers = new double[] { 5.0 };

    final OptionalDouble result = median.apply(numbers);

    assertThat(result).hasValue(5.0);
  }

  @Test
  void testApplyWithOddNumberOfElements() {
    final double[] numbers = new double[] { 3.0, 1.0, 2.0 };

    final OptionalDouble result = median.apply(numbers);

    assertThat(result).hasValue(2.0);
  }

  @Test
  void testApplyWithEvenNumberOfElements() {
    final double[] numbers = new double[] { 4.0, 1.0, 3.0, 2.0 };

    final OptionalDouble result = median.apply(numbers);

    assertThat(result).hasValue(2.5);
  }

  @Test
  void testApplyWithNegativeNumbers() {
    final double[] numbers = new double[] { -1.0, -3.0, -2.0 };

    final OptionalDouble result = median.apply(numbers);

    assertThat(result).hasValue(-2.0);
  }

  @Test
  void testApplyWithAlreadySortedArray() {
    final double[] numbers = new double[] { 1.0, 2.0, 3.0, 4.0 };

    final OptionalDouble result = median.apply(numbers);

    assertThat(result).hasValue(2.5);
  }

  @Test
  void testApplyWithDoubleStream() {
    final DoubleStream doubleStream = DoubleStream.of(5.0, 3.0, 4.0, 1.0, 2.0);

    final OptionalDouble result = median.apply(doubleStream);

    assertThat(result).hasValue(3.0);
  }

  @Test
  void testApplyWithEmptyDoubleStream() {
    final DoubleStream doubleStream = DoubleStream.empty();

    final OptionalDouble result = median.apply(doubleStream);

    assertThat(result).isEmpty();
  }
}
