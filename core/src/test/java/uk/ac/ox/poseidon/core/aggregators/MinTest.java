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

class MinTest {

    /**
     * The Min class provides a method to find the minimum value from a given {@link DoubleStream}.
     * This test class validates the functionality of the {@link Min#apply(DoubleStream)} method.
     */

    private final Min minAggregator = new Min();

    @Test
    void testApplyWithNonEmptyStream() {
        // Arrange
        final DoubleStream doubleStream = DoubleStream.of(3.5, 2.1, 4.8, 1.2, 5.6);

        // Act
        final OptionalDouble result = minAggregator.apply(doubleStream);

        // Assert
        assertTrue(result.isPresent(), "Expected result to be present");
        assertEquals(1.2, result.getAsDouble(), 0.001, "Minimum value is incorrect");
    }

    @Test
    void testApplyWithEmptyStream() {
        // Arrange
        final DoubleStream doubleStream = DoubleStream.empty();

        // Act
        final OptionalDouble result = minAggregator.apply(doubleStream);

        // Assert
        assertFalse(result.isPresent(), "Expected result to be empty");
    }

    @Test
    void testApplyWithSingleElementStream() {
        // Arrange
        final DoubleStream doubleStream = DoubleStream.of(7.9);

        // Act
        final OptionalDouble result = minAggregator.apply(doubleStream);

        // Assert
        assertTrue(result.isPresent(), "Expected result to be present");
        assertEquals(
            7.9,
            result.getAsDouble(),
            0.001,
            "Minimum value for single element stream is incorrect"
        );
    }

    @Test
    void testApplyWithNegativeValues() {
        // Arrange
        final DoubleStream doubleStream = DoubleStream.of(-3.4, -1.2, -7.8, -0.5);

        // Act
        final OptionalDouble result = minAggregator.apply(doubleStream);

        // Assert
        assertTrue(result.isPresent(), "Expected result to be present");
        assertEquals(
            -7.8,
            result.getAsDouble(),
            0.001,
            "Minimum value with negative numbers is incorrect"
        );
    }

    @Test
    void testApplyWithMixedSignValues() {
        // Arrange
        final DoubleStream doubleStream = DoubleStream.of(4.5, -2.3, 0.0, 3.1, -5.9);

        // Act
        final OptionalDouble result = minAggregator.apply(doubleStream);

        // Assert
        assertTrue(result.isPresent(), "Expected result to be present");
        assertEquals(
            -5.9,
            result.getAsDouble(),
            0.001,
            "Minimum value for mixed sign values is incorrect"
        );
    }
}
