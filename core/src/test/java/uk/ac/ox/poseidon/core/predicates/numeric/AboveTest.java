/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025 CoHESyS Lab cohesys.lab@gmail.com
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
 *
 */

package uk.ac.ox.poseidon.core.predicates.numeric;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AboveTest {

    /**
     * The Above class represents a Predicate<Double> that checks whether a given value is above a
     * certain threshold. The test(Double value) method returns true if the provided value is
     * greater than the threshold, and false otherwise.
     */

    @Test
    void testValueAboveThresholdShouldReturnTrue() {
        // Arrange
        final double threshold = 10.0;
        final Above above = new Above(threshold);

        // Act
        final boolean result = above.test(15.0);

        // Assert
        assertTrue(
            result,
            "Expected the test to return true when the value is above the threshold"
        );
    }

    @Test
    void testValueBelowThresholdShouldReturnFalse() {
        // Arrange
        final double threshold = 10.0;
        final Above above = new Above(threshold);

        // Act
        final boolean result = above.test(5.0);

        // Assert
        assertFalse(
            result,
            "Expected the test to return false when the value is below the threshold"
        );
    }

    @Test
    void testValueEqualToThresholdShouldReturnFalse() {
        // Arrange
        final double threshold = 10.0;
        final Above above = new Above(threshold);

        // Act
        final boolean result = above.test(10.0);

        // Assert
        assertFalse(
            result,
            "Expected the test to return false when the value is equal to the threshold"
        );
    }

    @Test
    void testValueIsNotNull() {
        // Arrange
        final double threshold = 10.0;
        final Above above = new Above(threshold);

        // Act & Assert
        assertDoesNotThrow(
            () -> above.test(12.0),
            "Expected the test to execute without throwing a NullPointerException when the value " +
                "is not null"
        );
    }

    @Test
    void testValueIsNullShouldThrowException() {
        // Arrange
        final double threshold = 10.0;
        final Above above = new Above(threshold);

        // Act & Assert
        assertThrows(
            NullPointerException.class,
            () -> above.test(null),
            "Expected the test to throw NullPointerException when the value is null"
        );
    }
}
