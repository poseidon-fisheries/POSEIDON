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

package uk.ac.ox.poseidon.regulations.predicates.logical;

import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.agents.behaviours.Action;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.Simulation;

import java.util.List;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AllOfFactoryTest {

    /**
     * Tests the `newInstance` method in the `AllOfFactory` class. This method creates a new
     * instance of the `AllOf` object containing a list of predicates.
     */

    @Test
    void testNewInstanceWithSinglePredicate() {
        // Arrange
        final Simulation mockSimulation = mock(Simulation.class);
        final Predicate<Action> mockPredicate = mock(Predicate.class);
        final Factory<Predicate<Action>> mockFactory = mock(Factory.class);

        when(mockFactory.get(mockSimulation)).thenReturn(mockPredicate);
        final AllOfFactory allOfFactory = new AllOfFactory(List.of(mockFactory));

        // Act
        final AllOf allOf = allOfFactory.newInstance(mockSimulation);

        // Assert
        assertNotNull(allOf, "The resulting AllOf instance should not be null");
        assertEquals(
            1,
            allOf.getPredicates().count(),
            "The AllOf instance should contain exactly one predicate"
        );
        assertEquals(
            mockPredicate, allOf.getPredicates().findFirst().orElse(null),
            "The predicate within AllOf should match the predicate returned by the factory"
        );
    }

    @Test
    void testNewInstanceWithMultiplePredicates() {
        // Arrange
        final Simulation mockSimulation = mock(Simulation.class);
        final Predicate<Action> mockPredicate1 = mock(Predicate.class);
        final Predicate<Action> mockPredicate2 = mock(Predicate.class);
        final Factory<Predicate<Action>> mockFactory1 = mock(Factory.class);
        final Factory<Predicate<Action>> mockFactory2 = mock(Factory.class);

        when(mockFactory1.get(mockSimulation)).thenReturn(mockPredicate1);
        when(mockFactory2.get(mockSimulation)).thenReturn(mockPredicate2);
        final AllOfFactory allOfFactory = new AllOfFactory(List.of(mockFactory1, mockFactory2));

        // Act
        final AllOf allOf = allOfFactory.newInstance(mockSimulation);

        // Assert
        assertNotNull(allOf, "The resulting AllOf instance should not be null");
        assertEquals(
            2,
            allOf.getPredicates().count(),
            "The AllOf instance should contain exactly two predicates"
        );
    }

    @Test
    void testNewInstanceWithEmptyPredicatesList() {
        // Arrange
        final Simulation mockSimulation = mock(Simulation.class);
        final AllOfFactory allOfFactory = new AllOfFactory(List.of());

        // Act
        final AllOf allOf = allOfFactory.newInstance(mockSimulation);

        // Assert
        assertNotNull(allOf, "The resulting AllOf instance should not be null");
        assertEquals(
            0,
            allOf.getPredicates().count(),
            "The AllOf instance should contain no predicates"
        );
    }

    @Test
    void testNewInstanceHandlesNullFactoryResponseGracefully() {
        // Arrange
        final Simulation mockSimulation = mock(Simulation.class);
        final Factory<Predicate<Action>> mockFactory = mock(Factory.class);

        when(mockFactory.get(mockSimulation)).thenReturn(null);
        final AllOfFactory allOfFactory = new AllOfFactory(List.of(mockFactory));

        // Act
        final AllOf allOf = allOfFactory.newInstance(mockSimulation);

        // Assert
        assertNotNull(allOf, "The resulting AllOf instance should not be null");
        assertEquals(
            1,
            allOf.getPredicates().count(),
            "The AllOf instance should contain exactly one predicate"
        );
        assertNull(
            allOf.getPredicates().findFirst().orElse(null),
            "The predicate within AllOf should be null since the factory returned null"
        );
    }
}
