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
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.scopes.Scope;
import uk.ac.ox.poseidon.core.utils.ObjectFactory;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SuppressWarnings("unchecked")
class AnyOfFactoryTest {

    /**
     * Tests the `get` method of the AnyOfFactory class. Verifies that the method creates an `AnyOf`
     * instance correctly when a valid list of factories for `Predicate<Object>` is provided and
     * preserves predicate order.
     */
    @Test
    void testGetWithValidPredicateFactories() {
        // Arrange
        final Predicate<Object> predicate1 = value -> true;
        final Predicate<Object> predicate2 = value -> false;
        final ObjectFactory<Predicate<Object>> factory1 = new ObjectFactory<>(predicate1);
        final ObjectFactory<Predicate<Object>> factory2 = new ObjectFactory<>(predicate2);

        final AnyOfFactory<Scope, Object> anyOfFactory =
            new AnyOfFactory<>(List.of(factory1, factory2));

        // Act
        final AnyOf<Object> result = anyOfFactory.get(Scope.GLOBAL_SCOPE);

        // Assert
        assertThat(result.getPredicates().toList())
            .as("Predicates should be preserved in the factory order")
            .containsExactly(predicate1, predicate2);
    }

    /**
     * Tests the `get` method with an empty list of factories. Verifies that an `AnyOf` instance is
     * still created successfully.
     */
    @Test
    void testGetWithEmptyFactoryList() {
        // Arrange
        final AnyOfFactory<Scope, Object> anyOfFactory =
            new AnyOfFactory<>(Collections.emptyList());

        // Act
        final AnyOf<Object> result = anyOfFactory.get(Scope.GLOBAL_SCOPE);

        // Assert
        assertThat(result)
            .as("The resulting AnyOf instance should not be null even with empty list.")
            .isNotNull();
        assertThat(result.test("value"))
            .as("AnyOf with no predicates should return false")
            .isFalse();
    }

    /**
     * Tests the `get` method to ensure it throws a NullPointerException when one of the factories
     * provides a null predicate.
     */
    @Test
    void testGetWithNullPredicateThrowsException() {
        // Arrange
        final Factory<Scope, Predicate<Object>> nullFactory = scope -> null;

        final AnyOfFactory<Scope, Object> anyOfFactory =
            new AnyOfFactory<>(List.of(nullFactory));

        // Act & Assert
        final AnyOf<Object> result = anyOfFactory.get(Scope.GLOBAL_SCOPE);
        assertThatThrownBy(() -> result.test("value"))
            .isInstanceOf(NullPointerException.class)
            .as("Expected AnyOf to throw NullPointerException when a predicate is null.");
    }

    /**
     * Tests the `get` method with multiple valid predicate factories to ensure AnyOf returns true
     * when at least one predicate matches.
     */
    @Test
    void testGetWithMultipleValidFactories() {
        // Arrange
        final ObjectFactory<Predicate<String>> factory1 =
            new ObjectFactory<>(value -> false);
        final ObjectFactory<Predicate<String>> factory2 =
            new ObjectFactory<>(value -> true);
        final ObjectFactory<Predicate<String>> factory3 =
            new ObjectFactory<>(value -> false);

        final AnyOfFactory<Scope, String> anyOfFactory =
            new AnyOfFactory<>(List.of(factory1, factory2, factory3));

        // Act
        final AnyOf<String> result = anyOfFactory.get(Scope.GLOBAL_SCOPE);

        // Assert
        assertThat(result.test("value"))
            .as("AnyOf should return true when any predicate is true")
            .isTrue();
    }

}
