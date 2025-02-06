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

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import uk.ac.ox.poseidon.agents.behaviours.Action;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.Simulation;

import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NotFactoryTest {

    /**
     * Tests the `newInstance` method of the NotFactory class. Verifies that the method creates a
     * `Not` instance correctly when a valid `Factory` for a `Predicate<Action>` is provided and is
     * functional.
     */
    @Test
    void testNewInstanceWithValidPredicateFactory() {
        // Arrange
        final Factory<Predicate<Action>> mockFactory = mock(Factory.class);
        final Predicate<Action> mockPredicate = mock(Predicate.class);
        final Simulation mockSimulation = mock(Simulation.class);

        when(mockFactory.get(Mockito.any())).thenReturn(mockPredicate);

        final NotFactory notFactory = new NotFactory(mockFactory);

        // Act
        final Not result = notFactory.newInstance(mockSimulation);

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
        final Factory<Predicate<Action>> mockFactory = mock(Factory.class);
        final Simulation mockSimulation = mock(Simulation.class);

        when(mockFactory.get(Mockito.any())).thenReturn(null);

        final NotFactory notFactory = new NotFactory(mockFactory);

        // Act & Assert
        assertThrows(
            NullPointerException.class, () -> notFactory.newInstance(mockSimulation),
            "Expected Not to throw NullPointerException when the predicate is null."
        );
    }
}
