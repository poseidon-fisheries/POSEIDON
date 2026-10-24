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

class MaxAggregatorTest {

  /**
   * The Max class implements the Aggregator interface and provides a method to
   * compute the maximum value from a given DoubleStream.
   *
   * <p>
   * Method being tested: {@link MaxAggregator#apply(DoubleStream)}.
   */

  @Test
  void apply_emptyStream_returnsEmptyOptional() {
    final MaxAggregator maxAggregator = new MaxAggregator();
    final DoubleStream emptyStream = DoubleStream.empty();

    final OptionalDouble result = maxAggregator.apply(emptyStream);

    assertThat(result).isEmpty();
  }

  @Test
  void apply_singleElementStream_returnsElementAsMax() {
    final MaxAggregator maxAggregator = new MaxAggregator();
    final DoubleStream singleElementStream = DoubleStream.of(42.0);

    final OptionalDouble result = maxAggregator.apply(singleElementStream);

    assertThat(result).hasValueCloseTo(42.0, within(0.0001));
  }

  @Test
  void apply_multipleElements_returnsMaximumValue() {
    final MaxAggregator maxAggregator = new MaxAggregator();
    final DoubleStream multipleElementsStream = DoubleStream.of(
      1.0,
      2.0,
      3.0,
      42.0,
      5.0
    );

    final OptionalDouble result = maxAggregator.apply(multipleElementsStream);

    assertThat(result).hasValueCloseTo(42.0, within(0.0001));
  }

  @Test
  void apply_negativeNumbers_returnsMaximumValue() {
    final MaxAggregator maxAggregator = new MaxAggregator();
    final DoubleStream negativeNumbersStream = DoubleStream.of(
      -10.0,
      -5.0,
      -20.0,
      -3.0
    );

    final OptionalDouble result = maxAggregator.apply(negativeNumbersStream);

    assertThat(result).hasValueCloseTo(-3.0, within(0.0001));
  }

  @Test
  void apply_mixedPositiveAndNegativeNumbers_returnsMaximumValue() {
    final MaxAggregator maxAggregator = new MaxAggregator();
    final DoubleStream mixedNumbersStream = DoubleStream.of(
      -1.0,
      -5.0,
      0.0,
      10.0,
      -3.0
    );

    final OptionalDouble result = maxAggregator.apply(mixedNumbersStream);

    assertThat(result).hasValueCloseTo(10.0, within(0.0001));
  }
}
