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

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NotTest {

    @Test
    void testWhenInnerPredicateReturnsTrue() {
        // Arrange
        final Predicate<Object> innerPredicate = Mockito.mock(Predicate.class);
        Mockito.when(innerPredicate.test(Mockito.any())).thenReturn(true);
        final Not notPredicate = new Not(innerPredicate);
        final Object mockObject = Mockito.mock(Object.class);

        // Act
        final boolean result = notPredicate.test(mockObject);

        // Assert
        assertFalse(
            result,
            "The Not predicate should return false when the inner predicate returns true."
        );
    }

    @Test
    void testWhenInnerPredicateReturnsFalse() {
        // Arrange
        final Predicate<Object> innerPredicate = Mockito.mock(Predicate.class);
        Mockito.when(innerPredicate.test(Mockito.any())).thenReturn(false);
        final Not notPredicate = new Not(innerPredicate);
        final Object mockObject = Mockito.mock(Object.class);

        // Act
        final boolean result = notPredicate.test(mockObject);

        // Assert
        assertTrue(
            result,
            "The Not predicate should return true when the inner predicate returns false."
        );
    }
}
