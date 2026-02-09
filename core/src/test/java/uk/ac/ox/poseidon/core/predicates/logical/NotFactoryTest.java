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
import uk.ac.ox.poseidon.core.utils.ObjectFactory;

import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        final Predicate<Object> predicate = value -> true;
        final ObjectFactory<Predicate<Object>> factory = new ObjectFactory<>(predicate);

        final NotFactory<Scope, Object> notFactory = new NotFactory<>(factory);

        // Act
        final Not<Object> result = notFactory.get(Scope.GLOBAL_SCOPE);

        // Assert
        assertThat(result.test("value"))
            .as("Not should negate the underlying predicate result")
            .isFalse();
    }

    /**
     * Tests the `newInstance` method to ensure it throws a NullPointerException when the factory
     * provides a null predicate.
     */
    @Test
    void testNewInstanceWithNullPredicateThrowsException() {
        // Arrange
        final Factory<Scope, Predicate<Object>> nullFactory = scope -> null;

        final NotFactory<Scope, Object> notFactory = new NotFactory<>(nullFactory);

        // Act & Assert
        assertThatThrownBy(() -> notFactory.get(Scope.GLOBAL_SCOPE))
            .isInstanceOf(UncheckedExecutionException.class)
            .hasCauseInstanceOf(NullPointerException.class)
            .as("Expected Not to fail when the predicate factory returns null.");
    }
}
