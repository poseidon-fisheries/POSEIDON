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

import org.junit.jupiter.api.Test;

import java.util.OptionalDouble;
import java.util.stream.DoubleStream;

import static org.junit.jupiter.api.Assertions.*;

class MaxTest {

    /**
     * The Max class implements the Aggregator interface and provides a method to compute the
     * maximum value from a given DoubleStream.
     *
     * <p>Method being tested: {@link Max#apply(DoubleStream)}.
     */

    @Test
    void apply_emptyStream_returnsEmptyOptional() {
        // Arrange
        final Max maxAggregator = new Max();
        final DoubleStream emptyStream = DoubleStream.empty();

        // Act
        final OptionalDouble result = maxAggregator.apply(emptyStream);

        // Assert
        assertFalse(
            result.isPresent(),
            "Expected the result to be an empty OptionalDouble for an empty stream."
        );
    }

    @Test
    void apply_singleElementStream_returnsElementAsMax() {
        // Arrange
        final Max maxAggregator = new Max();
        final DoubleStream singleElementStream = DoubleStream.of(42.0);

        // Act
        final OptionalDouble result = maxAggregator.apply(singleElementStream);

        // Assert
        assertTrue(
            result.isPresent(),
            "Expected the result to contain a value for a stream with a single element."
        );
        assertEquals(
            42.0,
            result.getAsDouble(),
            0.0001,
            "The max value should be equal to the single element in the stream."
        );
    }

    @Test
    void apply_multipleElements_returnsMaximumValue() {
        // Arrange
        final Max maxAggregator = new Max();
        final DoubleStream multipleElementsStream = DoubleStream.of(1.0, 2.0, 3.0, 42.0, 5.0);

        // Act
        final OptionalDouble result = maxAggregator.apply(multipleElementsStream);

        // Assert
        assertTrue(
            result.isPresent(),
            "Expected the result to contain a value for a stream with multiple elements."
        );
        assertEquals(
            42.0,
            result.getAsDouble(),
            0.0001,
            "The max value should be the largest element in the stream."
        );
    }

    @Test
    void apply_negativeNumbers_returnsMaximumValue() {
        // Arrange
        final Max maxAggregator = new Max();
        final DoubleStream negativeNumbersStream = DoubleStream.of(-10.0, -5.0, -20.0, -3.0);

        // Act
        final OptionalDouble result = maxAggregator.apply(negativeNumbersStream);

        // Assert
        assertTrue(
            result.isPresent(),
            "Expected the result to contain a value for a stream with negative numbers."
        );
        assertEquals(
            -3.0,
            result.getAsDouble(),
            0.0001,
            "The max value should be the least negative (largest) element in the stream."
        );
    }

    @Test
    void apply_mixedPositiveAndNegativeNumbers_returnsMaximumValue() {
        // Arrange
        final Max maxAggregator = new Max();
        final DoubleStream mixedNumbersStream = DoubleStream.of(-1.0, -5.0, 0.0, 10.0, -3.0);

        // Act
        final OptionalDouble result = maxAggregator.apply(mixedNumbersStream);

        // Assert
        assertTrue(
            result.isPresent(),
            "Expected the result to contain a value for a stream with mixed positive and negative" +
                " numbers."
        );
        assertEquals(
            10.0,
            result.getAsDouble(),
            0.0001,
            "The max value should be the largest element in the stream."
        );
    }
}
