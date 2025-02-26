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

import java.util.function.ToDoubleFunction;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BelowTest {

    @Test
    void testObjectValueBelowThreshold() {
        // Arrange
        final double threshold = 100.0;
        final ToDoubleFunction<Object> doubleFunction = mock(ToDoubleFunction.class);
        final Object object = mock(Object.class);
        when(doubleFunction.applyAsDouble(object)).thenReturn(50.0);
        final Below below = new Below(threshold, doubleFunction);

        // Act
        final boolean result = below.test(object);

        // Assert
        assertTrue(
            result,
            "Expected the test to return true when object's value is below the threshold"
        );
    }

    @Test
    void testObjectValueEqualToThreshold() {
        // Arrange
        final double threshold = 100.0;
        final ToDoubleFunction<Object> doubleFunction = mock(ToDoubleFunction.class);
        final Object object = mock(Object.class);
        when(doubleFunction.applyAsDouble(object)).thenReturn(100.0);
        final Below below = new Below(threshold, doubleFunction);

        // Act
        final boolean result = below.test(object);

        // Assert
        assertFalse(
            result,
            "Expected the test to return false when object's value equals the threshold"
        );
    }

    @Test
    void testObjectValueAboveThreshold() {
        // Arrange
        final double threshold = 100.0;
        final ToDoubleFunction<Object> doubleFunction = mock(ToDoubleFunction.class);
        final Object object = mock(Object.class);
        when(doubleFunction.applyAsDouble(object)).thenReturn(150.0);
        final Below below = new Below(threshold, doubleFunction);

        // Act
        final boolean result = below.test(object);

        // Assert
        assertFalse(
            result,
            "Expected the test to return false when object's value is above the threshold"
        );
    }

    @Test
    void testNullObjectThrowsException() {
        // Arrange
        final double threshold = 100.0;
        final ToDoubleFunction<Object> doubleFunction = mock(ToDoubleFunction.class);
        final Below below = new Below(threshold, doubleFunction);

        // Act & Assert
        assertThrows(
            NullPointerException.class,
            () -> below.test(null),
            "Expected NullPointerException when object is null"
        );
    }
}
