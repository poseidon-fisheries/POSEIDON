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

class MeanAggregatorTest {

  /**
   * Class under test: Mean Method under test: apply(DoubleStream doubleStream)
   * <p>
   * This method computes the arithmetic mean (average) of the provided stream
   * of doubles. If the stream is empty, the method returns an empty
   * OptionalDouble.
   */

  @Test
  void testApplyWithNonEmptyStream() {
    final MeanAggregator meanAggreagator = new MeanAggregator();
    final DoubleStream stream = DoubleStream.of(1.0, 2.0, 3.0, 4.0, 5.0);

    final OptionalDouble result = meanAggreagator.apply(stream);

    assertThat(result).hasValueCloseTo(3.0, within(0.0001));
  }

  @Test
  void testApplyWithEmptyStream() {
    final MeanAggregator meanAggreagator = new MeanAggregator();
    final DoubleStream stream = DoubleStream.empty();

    final OptionalDouble result = meanAggreagator.apply(stream);

    assertThat(result).isEmpty();
  }

  @Test
  void testApplyWithSingleElementStream() {
    final MeanAggregator meanAggreagator = new MeanAggregator();
    final DoubleStream stream = DoubleStream.of(42.0);

    final OptionalDouble result = meanAggreagator.apply(stream);

    assertThat(result).hasValueCloseTo(42.0, within(0.0001));
  }

  @Test
  void testApplyWithNegativeNumbers() {
    final MeanAggregator meanAggreagator = new MeanAggregator();
    final DoubleStream stream = DoubleStream.of(-10.0, -20.0, -30.0);

    final OptionalDouble result = meanAggreagator.apply(stream);

    assertThat(result).hasValueCloseTo(-20.0, within(0.0001));
  }

  @Test
  void testApplyWithMixedNumbers() {
    final MeanAggregator meanAggreagator = new MeanAggregator();
    final DoubleStream stream = DoubleStream.of(-10.0, 0.0, 10.0);

    final OptionalDouble result = meanAggreagator.apply(stream);

    assertThat(result).hasValueCloseTo(0.0, within(0.0001));
  }
}
