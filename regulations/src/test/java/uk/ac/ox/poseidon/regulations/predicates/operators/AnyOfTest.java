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

package uk.ac.ox.poseidon.regulations.predicates.operators;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.agents.behaviours.Action;

import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AnyOfTest {

    /**
     * Tests the {@link AnyOf} class, which holds a collection of predicates and evaluates if any of
     * them return true when applied to a given {@link Action}.
     */

    @Test
    void test_whenAnyPredicateMatches_shouldReturnTrue() {
        // Arrange
        final Action action = mock(Action.class);
        final Predicate<Action> predicate1 = mock(Predicate.class);
        final Predicate<Action> predicate2 = mock(Predicate.class);

        when(predicate1.test(action)).thenReturn(false);
        when(predicate2.test(action)).thenReturn(true);

        final AnyOf anyOf = new AnyOf(ImmutableList.of(predicate1, predicate2));

        // Act
        final boolean result = anyOf.test(action);

        // Assert
        assertTrue(result);
    }

    @Test
    void test_whenNoPredicatesMatch_shouldReturnFalse() {
        // Arrange
        final Action action = mock(Action.class);
        final Predicate<Action> predicate1 = mock(Predicate.class);
        final Predicate<Action> predicate2 = mock(Predicate.class);

        when(predicate1.test(action)).thenReturn(false);
        when(predicate2.test(action)).thenReturn(false);

        final AnyOf anyOf = new AnyOf(ImmutableList.of(predicate1, predicate2));

        // Act
        final boolean result = anyOf.test(action);

        // Assert
        assertFalse(result);
    }

    @Test
    void test_whenNoPredicatesProvided_shouldReturnFalse() {
        // Arrange
        final Action action = mock(Action.class);

        final AnyOf anyOf = new AnyOf(ImmutableList.of());

        // Act
        final boolean result = anyOf.test(action);

        // Assert
        assertFalse(result);
    }

    @Test
    void test_whenSinglePredicateMatches_shouldReturnTrue() {
        // Arrange
        final Action action = mock(Action.class);
        final Predicate<Action> predicate = mock(Predicate.class);

        when(predicate.test(action)).thenReturn(true);

        final AnyOf anyOf = new AnyOf(ImmutableList.of(predicate));

        // Act
        final boolean result = anyOf.test(action);

        // Assert
        assertTrue(result);
    }

    @Test
    void test_whenSinglePredicateDoesNotMatch_shouldReturnFalse() {
        // Arrange
        final Action action = mock(Action.class);
        final Predicate<Action> predicate = mock(Predicate.class);

        when(predicate.test(action)).thenReturn(false);

        final AnyOf anyOf = new AnyOf(ImmutableList.of(predicate));

        // Act
        final boolean result = anyOf.test(action);

        // Assert
        assertFalse(result);
    }
}
