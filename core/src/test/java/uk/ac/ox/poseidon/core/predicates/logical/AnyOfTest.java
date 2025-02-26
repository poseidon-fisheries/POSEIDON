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

package uk.ac.ox.poseidon.core.predicates.logical;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;

import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AnyOfTest {

    @Test
    void test_allPredicatesReturnFalse_shouldReturnFalse() {
        // Arrange
        final Object object = mock(Object.class);
        final Predicate<Object> falsePredicate1 = act -> false;
        final Predicate<Object> falsePredicate2 = act -> false;

        final AnyOf anyOf = new AnyOf(ImmutableList.of(falsePredicate1, falsePredicate2));

        // Act
        final boolean result = anyOf.test(object);

        // Assert
        assertFalse(result, "Expected false when all predicates return false.");
    }

    @Test
    void test_mixedPredicates_someTrueSomeFalse_shouldReturnTrue() {
        // Arrange
        final Object object = mock(Object.class);
        final Predicate<Object> falsePredicate = act -> false;
        final Predicate<Object> truePredicate = act -> true;

        final AnyOf anyOf = new AnyOf(ImmutableList.of(falsePredicate, truePredicate));

        // Act
        final boolean result = anyOf.test(object);

        // Assert
        assertTrue(result, "Expected true when at least one predicate returns true.");
    }

    /**
     * Tests the {@link AnyOf} class, which holds a collection of predicates and evaluates if any of
     * them return true when applied to a given {@link Object}.
     */

    @Test
    void test_whenAnyPredicateMatches_shouldReturnTrue() {
        // Arrange
        final Object object = mock(Object.class);
        final Predicate<Object> predicate1 = mock(Predicate.class);
        final Predicate<Object> predicate2 = mock(Predicate.class);

        when(predicate1.test(object)).thenReturn(false);
        when(predicate2.test(object)).thenReturn(true);

        final AnyOf anyOf = new AnyOf(ImmutableList.of(predicate1, predicate2));

        // Act
        final boolean result = anyOf.test(object);

        // Assert
        assertTrue(result);
    }

    @Test
    void test_whenNoPredicatesMatch_shouldReturnFalse() {
        // Arrange
        final Object object = mock(Object.class);
        final Predicate<Object> predicate1 = mock(Predicate.class);
        final Predicate<Object> predicate2 = mock(Predicate.class);

        when(predicate1.test(object)).thenReturn(false);
        when(predicate2.test(object)).thenReturn(false);

        final AnyOf anyOf = new AnyOf(ImmutableList.of(predicate1, predicate2));

        // Act
        final boolean result = anyOf.test(object);

        // Assert
        assertFalse(result);
    }

    @Test
    void test_whenNoPredicatesProvided_shouldReturnFalse() {
        // Arrange
        final Object object = mock(Object.class);

        final AnyOf anyOf = new AnyOf(ImmutableList.of());

        // Act
        final boolean result = anyOf.test(object);

        // Assert
        assertFalse(result);
    }

    @Test
    void test_whenSinglePredicateMatches_shouldReturnTrue() {
        // Arrange
        final Object object = mock(Object.class);
        final Predicate<Object> predicate = mock(Predicate.class);

        when(predicate.test(object)).thenReturn(true);

        final AnyOf anyOf = new AnyOf(ImmutableList.of(predicate));

        // Act
        final boolean result = anyOf.test(object);

        // Assert
        assertTrue(result);
    }

    @Test
    void test_whenSinglePredicateDoesNotMatch_shouldReturnFalse() {
        // Arrange
        final Object object = mock(Object.class);
        final Predicate<Object> predicate = mock(Predicate.class);

        when(predicate.test(object)).thenReturn(false);

        final AnyOf anyOf = new AnyOf(ImmutableList.of(predicate));

        // Act
        final boolean result = anyOf.test(object);

        // Assert
        assertFalse(result);
    }
}
