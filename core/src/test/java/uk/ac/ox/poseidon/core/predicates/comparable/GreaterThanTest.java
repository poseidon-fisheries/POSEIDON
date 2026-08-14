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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GreaterThanTest {

    @Test
    void testValueAboveThresholdShouldReturnTrue() {
        final double threshold = 10.0;
        final GreaterThan<Double> greaterThan = new GreaterThan<>(() -> threshold);

        assertTrue(
            greaterThan.test(15.0),
            "Expected the test to return true when the value is above the threshold"
        );
    }

    @Test
    void testValueBelowThresholdShouldReturnFalse() {
        final double threshold = 10.0;
        final GreaterThan<Double> greaterThan = new GreaterThan<>(() -> threshold);

        assertFalse(
            greaterThan.test(5.0),
            "Expected the test to return false when the value is below the threshold"
        );
    }

    @Test
    void testValueEqualToThresholdShouldReturnFalse() {
        final double threshold = 10.0;
        final GreaterThan<Double> greaterThan = new GreaterThan<>(() -> threshold);

        assertFalse(
            greaterThan.test(10.0),
            "Expected the test to return false when the value is equal to the threshold"
        );
    }

    @Test
    void testValueIsNotNull() {
        final double threshold = 10.0;
        final GreaterThan<Double> greaterThan = new GreaterThan<>(() -> threshold);

        assertDoesNotThrow(
            () -> greaterThan.test(12.0),
            "Expected the test to execute without throwing a NullPointerException when the value " +
                "is not null"
        );
    }

    @Test
    void testValueIsNullShouldThrowException() {
        final double threshold = 10.0;
        final GreaterThan<Double> greaterThan = new GreaterThan<>(() -> threshold);

        assertThrows(
            NullPointerException.class,
            () -> greaterThan.test(null),
            "Expected the test to throw NullPointerException when the value is null"
        );
    }
}
