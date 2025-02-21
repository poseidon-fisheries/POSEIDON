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

package uk.ac.ox.poseidon.core.aggregators;

import org.junit.jupiter.api.Test;

import java.util.OptionalDouble;
import java.util.stream.DoubleStream;

import static org.junit.jupiter.api.Assertions.*;

class MeanTest {

    /**
     * Class under test: Mean Method under test: apply(DoubleStream doubleStream)
     * <p>
     * This method computes the arithmetic mean (average) of the provided stream of doubles. If the
     * stream is empty, the method returns an empty OptionalDouble.
     */

    @Test
    void testApplyWithNonEmptyStream() {
        final Mean mean = new Mean();
        final DoubleStream stream = DoubleStream.of(1.0, 2.0, 3.0, 4.0, 5.0);

        final OptionalDouble result = mean.apply(stream);

        assertTrue(result.isPresent(), "Result should be present for a non-empty stream");
        assertEquals(3.0, result.getAsDouble(), 0.0001, "The calculated mean should be correct");
    }

    @Test
    void testApplyWithEmptyStream() {
        final Mean mean = new Mean();
        final DoubleStream stream = DoubleStream.empty();

        final OptionalDouble result = mean.apply(stream);

        assertFalse(result.isPresent(), "Result should be empty for an empty stream");
    }

    @Test
    void testApplyWithSingleElementStream() {
        final Mean mean = new Mean();
        final DoubleStream stream = DoubleStream.of(42.0);

        final OptionalDouble result = mean.apply(stream);

        assertTrue(result.isPresent(), "Result should be present for a single-element stream");
        assertEquals(
            42.0,
            result.getAsDouble(),
            0.0001,
            "The calculated mean should match the single element"
        );
    }

    @Test
    void testApplyWithNegativeNumbers() {
        final Mean mean = new Mean();
        final DoubleStream stream = DoubleStream.of(-10.0, -20.0, -30.0);

        final OptionalDouble result = mean.apply(stream);

        assertTrue(
            result.isPresent(),
            "Result should be present for a non-empty stream with negative numbers"
        );
        assertEquals(
            -20.0,
            result.getAsDouble(),
            0.0001,
            "The mean of negative numbers should be calculated correctly"
        );
    }

    @Test
    void testApplyWithMixedNumbers() {
        final Mean mean = new Mean();
        final DoubleStream stream = DoubleStream.of(-10.0, 0.0, 10.0);

        final OptionalDouble result = mean.apply(stream);

        assertTrue(result.isPresent(), "Result should be present for a mixed stream of numbers");
        assertEquals(
            0.0,
            result.getAsDouble(),
            0.0001,
            "The calculated mean for mixed numbers should be correct"
        );
    }
}
