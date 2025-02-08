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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AboveTest {

    @Test
    void testAboveThreshold() {
        // Arrange
        final double threshold = 50.0;
        final Action mockAction = mock(Action.class);
        final ToDoubleFunction<Action> mockDoubleFunction = mock(ToDoubleFunction.class);
        when(mockDoubleFunction.applyAsDouble(mockAction)).thenReturn(60.0); // Value above
        // threshold

        final Above predicate = new Above(threshold, mockDoubleFunction);

        // Act
        final boolean result = predicate.test(mockAction);

        // Assert
        assertTrue(result, "Expected the result to be true when the value is above the threshold.");
    }

    @Test
    void testEqualToThreshold() {
        // Arrange
        final double threshold = 50.0;
        final Action mockAction = mock(Action.class);
        final ToDoubleFunction<Action> mockDoubleFunction = mock(ToDoubleFunction.class);
        when(mockDoubleFunction.applyAsDouble(mockAction)).thenReturn(50.0); // Value equal to
        // threshold

        final Above predicate = new Above(threshold, mockDoubleFunction);

        // Act
        final boolean result = predicate.test(mockAction);

        // Assert
        assertFalse(
            result,
            "Expected the result to be false when the value is equal to the threshold."
        );
    }

    @Test
    void testBelowThreshold() {
        // Arrange
        final double threshold = 50.0;
        final Action mockAction = mock(Action.class);
        final ToDoubleFunction<Action> mockDoubleFunction = mock(ToDoubleFunction.class);
        when(mockDoubleFunction.applyAsDouble(mockAction)).thenReturn(40.0); // Value below
        // threshold

        final Above predicate = new Above(threshold, mockDoubleFunction);

        // Act
        final boolean result = predicate.test(mockAction);

        // Assert
        assertFalse(
            result,
            "Expected the result to be false when the value is below the threshold."
        );
    }

    @Test
    void testNullActionHandling() {
        // Arrange
        final double threshold = 50.0;
        final ToDoubleFunction<Action> mockDoubleFunction = mock(ToDoubleFunction.class);
        when(mockDoubleFunction.applyAsDouble(null)).thenThrow(NullPointerException.class);

        final Above predicate = new Above(threshold, mockDoubleFunction);

        // Act & Assert
        try {
            predicate.test(null);
        } catch (final NullPointerException e) {
            assertTrue(true, "NullPointerException was thrown as expected.");
        }
    }
}
