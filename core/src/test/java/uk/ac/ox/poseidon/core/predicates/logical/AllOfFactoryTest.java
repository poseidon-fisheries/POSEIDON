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

import com.google.common.util.concurrent.UncheckedExecutionException;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.scopes.Scope;
import uk.ac.ox.poseidon.core.utils.ConstantFactory;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SuppressWarnings("unchecked")
class AllOfFactoryTest {

    /**
     * Tests the `get` method with valid predicate factories. Verifies that the predicate order is
     * preserved.
     */
    @Test
    void getWithPredicateFactoriesPreservesOrder() {
        // Arrange
        final Predicate<Object> predicate1 = value -> true;
        final Predicate<Object> predicate2 = value -> false;
        final ConstantFactory<Predicate<Object>> factory1 = new ConstantFactory<>(predicate1);
        final ConstantFactory<Predicate<Object>> factory2 = new ConstantFactory<>(predicate2);

        final AllOfFactory<Scope, Object> allOfFactory =
            new AllOfFactory<>(List.of(factory1, factory2));

        // Act
        final AllOf<Object> result = allOfFactory.get(Scope.GLOBAL_SCOPE);

        // Assert
        assertThat(result.getPredicates().toList())
            .as("Predicates should be preserved in the factory order")
            .containsExactly(predicate1, predicate2);
    }

    /**
     * Tests the `get` method with an empty list of factories. Verifies that an `AllOf` instance is
     * still created successfully.
     */
    @Test
    void getWithEmptyFactoryListCreatesAllOfInstance() {
        // Arrange
        final AllOfFactory<Scope, Object> allOfFactory =
            new AllOfFactory<>(Collections.emptyList());

        // Act
        final AllOf<Object> result = allOfFactory.get(Scope.GLOBAL_SCOPE);

        // Assert
        assertThat(result)
            .as("The resulting AllOf instance should not be null even with empty list")
            .isNotNull();
    }

    /**
     * Tests the `get` method to ensure the resulting AllOf evaluates all predicates.
     */
    @Test
    void getWithPredicateFactoriesCombinesPredicates() {
        // Arrange
        final ConstantFactory<Predicate<String>> factory1 =
            new ConstantFactory<>(value -> true);
        final ConstantFactory<Predicate<String>> factory2 =
            new ConstantFactory<>(value -> false);

        final AllOfFactory<Scope, String> allOfFactory =
            new AllOfFactory<>(List.of(factory1, factory2));

        // Act
        final AllOf<String> result = allOfFactory.get(Scope.GLOBAL_SCOPE);

        // Assert
        assertThat(result.test("value"))
            .as("AllOf should only return true when all predicates are true")
            .isFalse();
    }

    /**
     * Tests the `get` method to ensure it throws a NullPointerException when one of the factories
     * provides a null predicate.
     */
    @Test
    void getWithNullPredicateThrowsNullPointerException() {
        // Arrange
        final Factory<Scope, Predicate<Object>> nullFactory = scope -> null;

        final AllOfFactory<Scope, Object> allOfFactory =
            new AllOfFactory<>(List.of(nullFactory));

        // Act & Assert
        assertThatThrownBy(() -> allOfFactory.get(Scope.GLOBAL_SCOPE))
            .isInstanceOf(UncheckedExecutionException.class)
            .hasCauseInstanceOf(NullPointerException.class)
            .as("Expected AllOf to fail when a predicate factory returns null");
    }

}
