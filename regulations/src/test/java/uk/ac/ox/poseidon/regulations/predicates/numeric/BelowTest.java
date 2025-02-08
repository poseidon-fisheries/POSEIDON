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

package uk.ac.ox.poseidon.regulations.predicates.numeric;

import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.agents.behaviours.Action;

import java.util.function.ToDoubleFunction;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BelowTest {

    @Test
    void testActionValueBelowThreshold() {
        // Arrange
        final double threshold = 100.0;
        final ToDoubleFunction<Action> doubleFunction = mock(ToDoubleFunction.class);
        final Action action = mock(Action.class);
        when(doubleFunction.applyAsDouble(action)).thenReturn(50.0);
        final Below below = new Below(threshold, doubleFunction);

        // Act
        final boolean result = below.test(action);

        // Assert
        assertTrue(
            result,
            "Expected the test to return true when action's value is below the threshold"
        );
    }

    @Test
    void testActionValueEqualToThreshold() {
        // Arrange
        final double threshold = 100.0;
        final ToDoubleFunction<Action> doubleFunction = mock(ToDoubleFunction.class);
        final Action action = mock(Action.class);
        when(doubleFunction.applyAsDouble(action)).thenReturn(100.0);
        final Below below = new Below(threshold, doubleFunction);

        // Act
        final boolean result = below.test(action);

        // Assert
        assertFalse(
            result,
            "Expected the test to return false when action's value equals the threshold"
        );
    }

    @Test
    void testActionValueAboveThreshold() {
        // Arrange
        final double threshold = 100.0;
        final ToDoubleFunction<Action> doubleFunction = mock(ToDoubleFunction.class);
        final Action action = mock(Action.class);
        when(doubleFunction.applyAsDouble(action)).thenReturn(150.0);
        final Below below = new Below(threshold, doubleFunction);

        // Act
        final boolean result = below.test(action);

        // Assert
        assertFalse(
            result,
            "Expected the test to return false when action's value is above the threshold"
        );
    }

    @Test
    void testNullActionThrowsException() {
        // Arrange
        final double threshold = 100.0;
        final ToDoubleFunction<Action> doubleFunction = mock(ToDoubleFunction.class);
        final Below below = new Below(threshold, doubleFunction);

        // Act & Assert
        assertThrows(
            NullPointerException.class,
            () -> below.test(null),
            "Expected NullPointerException when action is null"
        );
    }
}
