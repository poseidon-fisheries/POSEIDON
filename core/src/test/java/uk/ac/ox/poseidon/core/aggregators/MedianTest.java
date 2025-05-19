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

class MedianTest {

    /**
     * This class tests the `apply` method in the `Median` class. The `apply` method calculates the
     * median of a double array or a DoubleStream.
     */

    private final Median median = new Median();

    @Test
    void testApplyWithEmptyArray() {
        // Given
        final double[] numbers = new double[]{};

        // When
        final OptionalDouble result = median.apply(numbers);

        // Then
        assertTrue(result.isEmpty(), "Expected result to be empty for an empty array.");
    }

    @Test
    void testApplyWithSingleElement() {
        // Given
        final double[] numbers = new double[]{5.0};

        // When
        final OptionalDouble result = median.apply(numbers);

        // Then
        assertTrue(result.isPresent(), "Expected result to be present for a single-element array.");
        assertEquals(
            5.0,
            result.getAsDouble(),
            "Expected median to be 5.0 for a single-element array."
        );
    }

    @Test
    void testApplyWithOddNumberOfElements() {
        // Given
        final double[] numbers = new double[]{3.0, 1.0, 2.0};

        // When
        final OptionalDouble result = median.apply(numbers);

        // Then
        assertTrue(
            result.isPresent(),
            "Expected result to be present for an array with odd number of elements."
        );
        assertEquals(
            2.0,
            result.getAsDouble(),
            "Expected median to be 2.0 for the input array [3.0, 1.0, 2.0]."
        );
    }

    @Test
    void testApplyWithEvenNumberOfElements() {
        // Given
        final double[] numbers = new double[]{4.0, 1.0, 3.0, 2.0};

        // When
        final OptionalDouble result = median.apply(numbers);

        // Then
        assertTrue(
            result.isPresent(),
            "Expected result to be present for an array with even number of elements."
        );
        assertEquals(
            2.5,
            result.getAsDouble(),
            "Expected median to be 2.5 for the input array [4.0, 1.0, 3.0, 2.0]."
        );
    }

    @Test
    void testApplyWithNegativeNumbers() {
        // Given
        final double[] numbers = new double[]{-1.0, -3.0, -2.0};

        // When
        final OptionalDouble result = median.apply(numbers);

        // Then
        assertTrue(
            result.isPresent(),
            "Expected result to be present for an array with negative numbers."
        );
        assertEquals(
            -2.0,
            result.getAsDouble(),
            "Expected median to be -2.0 for the input array [-1.0, -3.0, -2.0]."
        );
    }

    @Test
    void testApplyWithAlreadySortedArray() {
        // Given
        final double[] numbers = new double[]{1.0, 2.0, 3.0, 4.0};

        // When
        final OptionalDouble result = median.apply(numbers);

        // Then
        assertTrue(
            result.isPresent(),
            "Expected result to be present for an already sorted array."
        );
        assertEquals(
            2.5,
            result.getAsDouble(),
            "Expected median to be 2.5 for the input array [1.0, 2.0, 3.0, 4.0]."
        );
    }

    @Test
    void testApplyWithDoubleStream() {
        // Given
        final DoubleStream doubleStream = DoubleStream.of(5.0, 3.0, 4.0, 1.0, 2.0);

        // When
        final OptionalDouble result = median.apply(doubleStream);

        // Then
        assertTrue(result.isPresent(), "Expected result to be present for a DoubleStream.");
        assertEquals(
            3.0,
            result.getAsDouble(),
            "Expected median to be 3.0 for the input DoubleStream [5.0, 3.0, 4.0, 1.0, 2.0]."
        );
    }

    @Test
    void testApplyWithEmptyDoubleStream() {
        // Given
        final DoubleStream doubleStream = DoubleStream.empty();

        // When
        final OptionalDouble result = median.apply(doubleStream);

        // Then
        assertTrue(result.isEmpty(), "Expected result to be empty for an empty DoubleStream.");
    }
}
