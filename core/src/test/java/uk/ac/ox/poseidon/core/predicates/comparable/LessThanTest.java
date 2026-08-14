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

class LessThanTest {

    @Test
    void testValueLessThanThreshold() {
        final double threshold = 10.0;
        final LessThan<Double> lessThan = new LessThan<>(() -> threshold);

        assertTrue(lessThan.test(5.0), "The value 5.0 should be less than the threshold 10.0");
    }

    @Test
    void testValueEqualsThreshold() {
        final double threshold = 10.0;
        final LessThan<Double> lessThan = new LessThan<>(() -> threshold);

        assertFalse(lessThan.test(10.0), "The value 10.0 should not be less than the threshold 10.0");
    }

    @Test
    void testValueAboveThreshold() {
        final double threshold = 10.0;
        final LessThan<Double> lessThan = new LessThan<>(() -> threshold);

        assertFalse(lessThan.test(15.0), "The value 15.0 should not be less than the threshold 10.0");
    }

    @Test
    void testNegativeValueLessThanNegativeThreshold() {
        final double threshold = -5.0;
        final LessThan<Double> lessThan = new LessThan<>(() -> threshold);

        assertTrue(lessThan.test(-10.0), "The value -10.0 should be less than the threshold -5.0");
    }

    @Test
    void testNegativeValueEqualsNegativeThreshold() {
        final double threshold = -5.0;
        final LessThan<Double> lessThan = new LessThan<>(() -> threshold);

        assertFalse(lessThan.test(-5.0), "The value -5.0 should not be less than the threshold -5.0");
    }

    @Test
    void testNegativeValueGreaterThanNegativeThreshold() {
        final double threshold = -10.0;
        final LessThan<Double> lessThan = new LessThan<>(() -> threshold);

        assertFalse(lessThan.test(-5.0), "The value -5.0 should not be less than the threshold -10.0");
    }

    @Test
    void testWithZeroValue() {
        final double threshold = 0.0;
        final LessThan<Double> lessThan = new LessThan<>(() -> threshold);

        assertTrue(lessThan.test(-1.0), "The value -1.0 should be less than the threshold 0.0");
    }

    @Test
    void testValueIsNull() {
        final double threshold = 10.0;
        final LessThan<Double> lessThan = new LessThan<>(() -> threshold);

        assertThrows(
            NullPointerException.class,
            () -> lessThan.test(null),
            "Passing null should throw NullPointerException"
        );
    }
}
