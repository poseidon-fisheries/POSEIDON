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
import static org.assertj.core.api.Assertions.within;

import java.util.OptionalDouble;
import java.util.stream.DoubleStream;
import org.junit.jupiter.api.Test;

class SumTest {

  /**
   * Tests for the {@link SumAggregator} class.
   *
   * <p>
   * The {@link SumAggregator#apply(DoubleStream)} method takes a
   * {@link DoubleStream} and returns the sum of all its elements wrapped in an
   * {@link OptionalDouble}. If the stream is empty, the result will still
   * return a valid {@link OptionalDouble} with its value set to 0.0 since the
   * sum of zero elements is conventionally 0.0.
   * </p>
   */

  @Test
  void testApplyWithNonEmptyStream() {
    final SumAggregator sumAggregator = new SumAggregator();
    final DoubleStream doubleStream = DoubleStream.of(1.2, 2.3, 3.4, 4.5);

    final OptionalDouble result = sumAggregator.apply(doubleStream);

    assertThat(result).hasValueCloseTo(11.4, within(1e-9));
  }

  @Test
  void testApplyWithEmptyStream() {
    final SumAggregator sumAggregator = new SumAggregator();
    final DoubleStream doubleStream = DoubleStream.empty();

    final OptionalDouble result = sumAggregator.apply(doubleStream);

    assertThat(result).hasValueCloseTo(0.0, within(1e-9));
  }

  @Test
  void testApplyWithSingleElementStream() {
    final SumAggregator sumAggregator = new SumAggregator();
    final DoubleStream doubleStream = DoubleStream.of(5.0);

    final OptionalDouble result = sumAggregator.apply(doubleStream);

    assertThat(result).hasValueCloseTo(5.0, within(1e-9));
  }

  @Test
  void testApplyWithNegativeNumbers() {
    final SumAggregator sumAggregator = new SumAggregator();
    final DoubleStream doubleStream = DoubleStream.of(-1.0, -2.5, -3.5);

    final OptionalDouble result = sumAggregator.apply(doubleStream);

    assertThat(result).hasValueCloseTo(-7.0, within(1e-9));
  }

  @Test
  void testApplyWithMixedNumbers() {
    final SumAggregator sumAggregator = new SumAggregator();
    final DoubleStream doubleStream = DoubleStream.of(-1.2, 2.3, -3.4, 4.5);

    final OptionalDouble result = sumAggregator.apply(doubleStream);

    assertThat(result).hasValueCloseTo(2.2, within(1e-9));
  }
}
