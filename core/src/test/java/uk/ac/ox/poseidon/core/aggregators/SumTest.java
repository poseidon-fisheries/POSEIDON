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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SumTest {

    /**
     * Tests for the {@link Sum} class.
     *
     * <p>
     * The {@link Sum#apply(DoubleStream)} method takes a {@link DoubleStream} and returns the sum
     * of all its elements wrapped in an {@link OptionalDouble}. If the stream is empty, the result
     * will still return a valid {@link OptionalDouble} with its value set to 0.0 since the sum of
     * zero elements is conventionally 0.0.
     * </p>
     */

    @Test
    void testApplyWithNonEmptyStream() {
        // Arrange
        final Sum sum = new Sum();
        final DoubleStream doubleStream = DoubleStream.of(1.2, 2.3, 3.4, 4.5);

        // Act
        final OptionalDouble result = sum.apply(doubleStream);

        // Assert
        assertTrue(result.isPresent(), "The result should be present for a non-empty stream.");
        assertEquals(
            11.4,
            result.getAsDouble(),
            1e-9,
            "The sum of the elements was not computed correctly."
        );
    }

    @Test
    void testApplyWithEmptyStream() {
        // Arrange
        final Sum sum = new Sum();
        final DoubleStream doubleStream = DoubleStream.empty();

        // Act
        final OptionalDouble result = sum.apply(doubleStream);

        // Assert
        assertTrue(result.isPresent(), "The result should be present even for an empty stream.");
        assertEquals(0.0, result.getAsDouble(), 1e-9, "The sum of an empty stream should be 0.0.");
    }

    @Test
    void testApplyWithSingleElementStream() {
        // Arrange
        final Sum sum = new Sum();
        final DoubleStream doubleStream = DoubleStream.of(5.0);

        // Act
        final OptionalDouble result = sum.apply(doubleStream);

        // Assert
        assertTrue(result.isPresent(), "The result should be present for a single-element stream.");
        assertEquals(
            5.0,
            result.getAsDouble(),
            1e-9,
            "The sum of a single-element stream was not computed correctly."
        );
    }

    @Test
    void testApplyWithNegativeNumbers() {
        // Arrange
        final Sum sum = new Sum();
        final DoubleStream doubleStream = DoubleStream.of(-1.0, -2.5, -3.5);

        // Act
        final OptionalDouble result = sum.apply(doubleStream);

        // Assert
        assertTrue(
            result.isPresent(),
            "The result should be present for a stream with negative numbers."
        );
        assertEquals(
            -7.0,
            result.getAsDouble(),
            1e-9,
            "The sum of the negative numbers was not computed correctly."
        );
    }

    @Test
    void testApplyWithMixedNumbers() {
        // Arrange
        final Sum sum = new Sum();
        final DoubleStream doubleStream = DoubleStream.of(-1.2, 2.3, -3.4, 4.5);

        // Act
        final OptionalDouble result = sum.apply(doubleStream);

        // Assert
        assertTrue(
            result.isPresent(),
            "The result should be present for a stream with mixed numbers."
        );
        assertEquals(
            2.2,
            result.getAsDouble(),
            1e-9,
            "The sum of the mixed numbers was not computed correctly."
        );
    }
}
