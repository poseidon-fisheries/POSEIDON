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

package uk.ac.ox.poseidon.agents.regulations.predicates.operators;

import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.agents.behaviours.Action;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.Simulation;

import java.util.List;
import java.util.function.Predicate;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AnyOfFactoryTest {

    /**
     * Test class for AnyOfFactory's `newInstance` method. Verifies that the method correctly
     * creates an instance of the AnyOf class where predicate factories are resolved with the
     * provided simulation and stored properly.
     */

    @Test
    void testNewInstance_ValidPredicates() {
        // Arrange
        final Simulation simulation = mock(Simulation.class);
        final Factory<Predicate<Action>> predicateFactory1 = mock(Factory.class);
        final Factory<Predicate<Action>> predicateFactory2 = mock(Factory.class);
        final Predicate<Action> predicate1 = mock(Predicate.class);
        final Predicate<Action> predicate2 = mock(Predicate.class);

        when(predicateFactory1.get(simulation)).thenReturn(predicate1);
        when(predicateFactory2.get(simulation)).thenReturn(predicate2);

        final List<Factory<? extends Predicate<Action>>> predicateFactories = List.of(
            predicateFactory1,
            predicateFactory2
        );
        final AnyOfFactory anyOfFactory = new AnyOfFactory(predicateFactories);

        // Act
        final AnyOf anyOf = anyOfFactory.newInstance(simulation);
        
        // Assert
        assertNotNull(anyOf);
        verify(predicateFactory1, times(1)).get(simulation);
        verify(predicateFactory2, times(1)).get(simulation);
        assertEquals(
            predicateFactories
                .stream()
                .map(factory -> factory.get(simulation))
                .collect(toImmutableList()),
            anyOf.getPredicates()
        );

    }

    @Test
    void testNewInstance_EmptyPredicates() {
        // Arrange
        final Simulation simulation = mock(Simulation.class);
        final AnyOfFactory anyOfFactory = new AnyOfFactory(List.of());

        // Act
        final AnyOf anyOf = anyOfFactory.newInstance(simulation);

        // Assert
        assertNotNull(anyOf);
        assertEquals(0, anyOf.getPredicates().size());
    }

    @Test
    void testNewInstance_NullPredicateFactory() {
        // Arrange
        final Simulation simulation = mock(Simulation.class);
        final Factory<Predicate<Action>> nullFactory = __ -> null;
        final AnyOfFactory anyOfFactory = new AnyOfFactory(List.of(nullFactory));

        // Act & Assert
        assertThrows(NullPointerException.class, () -> anyOfFactory.newInstance(simulation));
    }
}
