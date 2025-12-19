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
import uk.ac.ox.poseidon.core.scopes.Scope;

import java.util.List;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static uk.ac.ox.poseidon.core.scopes.Scope.GLOBAL_SCOPE;

@SuppressWarnings("unchecked")
class AllOfFactoryTest {

    /**
     * Tests for the AllOfFactory's newInstance method. AllOfFactory is responsible for creating
     * instances of the AllOf class, which combines multiple predicates into an "all-of" logical
     * combination.
     */

    @Test
    void newInstance_createsInstanceWithAllPredicates() {
        // Arrange
        final Predicate<Object> predicate1 = mock(Predicate.class);
        final Predicate<Object> predicate2 = mock(Predicate.class);

        final GlobalScopeFactory<Predicate<Object>> factoryMock1 = mock(GlobalScopeFactory.class);
        final GlobalScopeFactory<Predicate<Object>> factoryMock2 = mock(GlobalScopeFactory.class);

        when(factoryMock1.get()).thenReturn(predicate1);
        when(factoryMock2.get()).thenReturn(predicate2);

        final AllOfFactory<Scope, Object> allOfFactory =
            new AllOfFactory<>(List.of(factoryMock1, factoryMock2));

        // Act
        final AllOf<Object> allOf = allOfFactory.newInstance(GLOBAL_SCOPE);

        // Assert
        assertEquals(
            2,
            allOf.getPredicates().count(),
            "The AllOf instance should contain two predicates"
        );
        verify(factoryMock1, times(1)).get();
        verify(factoryMock2, times(1)).get();
    }

    @Test
    void newInstance_createsInstanceWithEmptyPredicateList() {
        // Arrange
        final AllOfFactory<Scope, Object> allOfFactory = new AllOfFactory<>(List.of());

        // Act
        final AllOf<Object> allOf = allOfFactory.get(GLOBAL_SCOPE);

        // Assert
        assertEquals(
            0,
            allOf.getPredicates().count(),
            "The AllOf instance should contain zero predicates"
        );
    }

    @Test
    void newInstance_predicateCalledOnlyOncePerFactory() {
        // Arrange
        final GlobalScopeFactory<Predicate<Object>> factoryMock = mock(GlobalScopeFactory.class);
        final Predicate<Object> predicateMock = mock(Predicate.class);

        when(factoryMock.get()).thenReturn(predicateMock);

        final AllOfFactory<Scope, Object> allOfFactory = new AllOfFactory<>(List.of(factoryMock));

        // Act
        allOfFactory.get(GLOBAL_SCOPE);

        // Assert
        verify(factoryMock, times(1)).get();
    }
}
