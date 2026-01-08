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
import uk.ac.ox.poseidon.core.GlobalScopeFactory;
import uk.ac.ox.poseidon.core.scopes.GlobalScope;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
class AllOfFactoryTest {

    /**
     * Tests the `get` method of the AllOfFactory class. Verifies that the method creates an `AllOf`
     * instance correctly when a valid list of factories for `Predicate<Object>` is provided.
     */
    @Test
    void getWithValidPredicateFactoriesCreatesAllOfInstance() {
        // Arrange
        final GlobalScopeFactory<Predicate<Object>> mockFactory1 =
            mock(GlobalScopeFactory.class);
        final GlobalScopeFactory<Predicate<Object>> mockFactory2 =
            mock(GlobalScopeFactory.class);
        final Predicate<Object> mockPredicate1 = mock(Predicate.class);
        final Predicate<Object> mockPredicate2 = mock(Predicate.class);
        final GlobalScope mockScope = mock(GlobalScope.class);

        when(mockFactory1.get(any())).thenReturn(mockPredicate1);
        when(mockFactory2.get(any())).thenReturn(mockPredicate2);

        final AllOfFactory<GlobalScope, Object> allOfFactory =
            new AllOfFactory<>(List.of(mockFactory1, mockFactory2));

        // Act
        final AllOf<Object> result = allOfFactory.get(mockScope);

        // Assert
        assertThat(result)
            .as("The resulting AllOf instance should not be null")
            .isNotNull();
    }

    /**
     * Tests the `get` method with an empty list of factories. Verifies that an `AllOf` instance is
     * still created successfully.
     */
    @Test
    void getWithEmptyFactoryListCreatesAllOfInstance() {
        // Arrange
        final AllOfFactory<GlobalScope, Object> allOfFactory =
            new AllOfFactory<>(Collections.emptyList());
        final GlobalScope mockScope = mock(GlobalScope.class);

        // Act
        final AllOf<Object> result = allOfFactory.get(mockScope);

        // Assert
        assertThat(result)
            .as("The resulting AllOf instance should not be null even with empty list")
            .isNotNull();
    }

    /**
     * Tests the `get` method to ensure it throws a NullPointerException when one of the factories
     * provides a null predicate.
     */
    @Test
    void getWithNullPredicateThrowsNullPointerException() {
        // Arrange
        final GlobalScopeFactory<Predicate<Object>> mockFactory =
            mock(GlobalScopeFactory.class);
        final GlobalScope mockScope = mock(GlobalScope.class);

        when(mockFactory.get(any())).thenReturn(null);

        final AllOfFactory<GlobalScope, Object> allOfFactory =
            new AllOfFactory<>(List.of(mockFactory));

        // Act & Assert
        assertThatThrownBy(() -> allOfFactory.get(mockScope))
            .isInstanceOf(NullPointerException.class)
            .as("Expected AllOf to throw NullPointerException when a predicate is null");
    }

    /**
     * Tests the `get` method with multiple valid predicate factories to ensure all predicates are
     * properly instantiated and combined into an AllOf instance.
     */
    @Test
    void getWithMultipleValidFactoriesCreatesAllOfInstance() {
        // Arrange
        final GlobalScopeFactory<Predicate<Object>> mockFactory1 =
            mock(GlobalScopeFactory.class);
        final GlobalScopeFactory<Predicate<Object>> mockFactory2 =
            mock(GlobalScopeFactory.class);
        final GlobalScopeFactory<Predicate<Object>> mockFactory3 =
            mock(GlobalScopeFactory.class);
        final Predicate<Object> mockPredicate1 = mock(Predicate.class);
        final Predicate<Object> mockPredicate2 = mock(Predicate.class);
        final Predicate<Object> mockPredicate3 = mock(Predicate.class);
        final GlobalScope mockScope = mock(GlobalScope.class);

        when(mockFactory1.get(any())).thenReturn(mockPredicate1);
        when(mockFactory2.get(any())).thenReturn(mockPredicate2);
        when(mockFactory3.get(any())).thenReturn(mockPredicate3);

        final AllOfFactory<GlobalScope, Object> allOfFactory =
            new AllOfFactory<>(List.of(mockFactory1, mockFactory2, mockFactory3));

        // Act
        final AllOf<Object> result = allOfFactory.get(mockScope);

        // Assert
        assertThat(result)
            .as("The resulting AllOf instance should not be null with multiple factories")
            .isNotNull();
    }

}
