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
import org.mockito.Mockito;
import uk.ac.ox.poseidon.agents.behaviours.Action;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.Simulation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AllOfFactoryTest {

    /**
     * Test class for AllOfFactory. AllOfFactory is responsible for creating instances of AllOf. The
     * method `newInstance` initializes an AllOf object with a collection of predicates by
     * retrieving them through factories in the context of a provided simulation.
     */

    @Test
    void testNewInstanceWithEmptyPredicates() {
        // Arrange
        final List<Factory<? extends Predicate<Action>>> predicateFactories = new ArrayList<>();
        final AllOfFactory allOfFactory = new AllOfFactory(predicateFactories);
        final Simulation mockSimulation = mock(Simulation.class);

        // Act
        final AllOf result = allOfFactory.newInstance(mockSimulation);

        // Assert
        assertEquals(0, result.getPredicates().size());
    }

    @Test
    void testNewInstanceWithSinglePredicate() {
        // Arrange
        final Predicate<Action> mockPredicate = mock(Predicate.class);
        final Factory<Predicate<Action>> mockFactory = mock(Factory.class);
        when(mockFactory.get(Mockito.any(Simulation.class))).thenReturn(mockPredicate);

        final List<Factory<? extends Predicate<Action>>> predicateFactories = List.of(mockFactory);
        final AllOfFactory allOfFactory = new AllOfFactory(predicateFactories);
        final Simulation mockSimulation = mock(Simulation.class);

        // Act
        final AllOf result = allOfFactory.newInstance(mockSimulation);

        // Assert
        assertEquals(1, result.getPredicates().size());
        assertEquals(mockPredicate, result.getPredicates().iterator().next());
    }

    @Test
    void testNewInstanceWithMultiplePredicates() {
        // Arrange
        final Predicate<Action> mockPredicate1 = mock(Predicate.class);
        final Predicate<Action> mockPredicate2 = mock(Predicate.class);

        final Factory<Predicate<Action>> mockFactory1 = mock(Factory.class);
        final Factory<Predicate<Action>> mockFactory2 = mock(Factory.class);

        when(mockFactory1.get(Mockito.any(Simulation.class))).thenReturn(mockPredicate1);
        when(mockFactory2.get(Mockito.any(Simulation.class))).thenReturn(mockPredicate2);

        final List<Factory<? extends Predicate<Action>>> predicateFactories = List.of(
            mockFactory1,
            mockFactory2
        );
        final AllOfFactory allOfFactory = new AllOfFactory(predicateFactories);
        final Simulation mockSimulation = mock(Simulation.class);

        // Act
        final AllOf result = allOfFactory.newInstance(mockSimulation);

        // Assert
        assertEquals(2, result.getPredicates().size());
        assertEquals(
            List.of(mockPredicate1, mockPredicate2),
            new ArrayList<>(result.getPredicates())
        );
    }
}
