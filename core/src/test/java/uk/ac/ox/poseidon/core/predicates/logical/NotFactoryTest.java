/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

package uk.ac.ox.poseidon.core.predicates.logical;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import uk.ac.ox.poseidon.core.GlobalScopeFactory;
import uk.ac.ox.poseidon.core.scopes.Scope;

import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.ac.ox.poseidon.core.scopes.Scope.GLOBAL_SCOPE;

@SuppressWarnings("unchecked")
class NotFactoryTest {

    /**
     * Tests the `newInstance` method of the NotFactory class. Verifies that the method creates a
     * `Not` instance correctly when a valid `Factory` for a `Predicate<Object>` is provided and is
     * functional.
     */
    @Test
    void testNewInstanceWithValidPredicateFactory() {
        // Arrange
        final GlobalScopeFactory<Predicate<Object>> mockFactory = mock(GlobalScopeFactory.class);
        final Predicate<Object> mockPredicate = mock(Predicate.class);

        when(mockFactory.get(Mockito.any())).thenReturn(mockPredicate);

        final NotFactory<Scope, Object> notFactory = new NotFactory<>(mockFactory);

        // Act
        final Not<Object> result = notFactory.get(GLOBAL_SCOPE);

        // Assert
        assertNotNull(result, "The resulting Not instance should not be null.");
    }

    /**
     * Tests the `newInstance` method to ensure it throws a NullPointerException when the factory
     * provides a null predicate.
     */
    @Test
    void testNewInstanceWithNullPredicateThrowsException() {
        // Arrange
        final GlobalScopeFactory<Predicate<Object>> mockFactory = mock(GlobalScopeFactory.class);

        when(mockFactory.get(Mockito.any())).thenReturn(null);

        final NotFactory<Scope, Object> notFactory = new NotFactory<>(mockFactory);

        // Act & Assert
        assertThrows(
            NullPointerException.class, () -> notFactory.get(GLOBAL_SCOPE),
            "Expected Not to throw NullPointerException when the predicate is null."
        );
    }
}
