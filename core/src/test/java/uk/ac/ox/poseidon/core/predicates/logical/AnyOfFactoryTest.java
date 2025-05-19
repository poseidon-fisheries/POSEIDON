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
import uk.ac.ox.poseidon.core.Simulation;

import java.util.List;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class AnyOfFactoryTest {

    /**
     * Tests the `newInstance` method of the `AnyOfFactory` class. This method is responsible for
     * creating a new instance of `AnyOf` by transforming a list of `Factory` objects into their
     * corresponding `Predicate<Object>` objects using a provided `Simulation`.
     */

    @Test
    void testNewInstance_WithSinglePredicate() {
        // Arrange
        final Simulation simulation = mock(Simulation.class);
        final Predicate<Object> mockPredicate = mock(Predicate.class);
        final Factory<Predicate<Object>> mockFactory = mock(Factory.class);
        when(mockFactory.get(simulation)).thenReturn(mockPredicate);

        final AnyOfFactory anyOfFactory = new AnyOfFactory(List.of(mockFactory));

        // Act
        final AnyOf result = anyOfFactory.newInstance(simulation);

        // Assert
        assertNotNull(result, "The result of newInstance should not be null");
        assertEquals(
            1,
            result.getPredicates().count(),
            "The resulting AnyOf should contain one predicate"
        );
        assertEquals(
            mockPredicate,
            result.getPredicates().findFirst().orElse(null),
            "The predicate in AnyOf should match the result of the Factory's get method"
        );
    }

    @Test
    void testNewInstance_WithMultiplePredicates() {
        // Arrange
        final Simulation simulation = mock(Simulation.class);

        final Predicate<Object> mockPredicate1 = mock(Predicate.class);
        final Factory<Predicate<Object>> mockFactory1 = mock(Factory.class);
        when(mockFactory1.get(simulation)).thenReturn(mockPredicate1);

        final Predicate<Object> mockPredicate2 = mock(Predicate.class);
        final Factory<Predicate<Object>> mockFactory2 = mock(Factory.class);
        when(mockFactory2.get(simulation)).thenReturn(mockPredicate2);

        final AnyOfFactory anyOfFactory = new AnyOfFactory(List.of(mockFactory1, mockFactory2));

        // Act
        final AnyOf result = anyOfFactory.newInstance(simulation);

        // Assert
        assertNotNull(result, "The result of newInstance should not be null");
        assertEquals(
            2,
            result.getPredicates().count(),
            "The resulting AnyOf should contain two predicates"
        );
    }

    @Test
    void testNewInstance_WhenNoPredicates() {
        // Arrange
        final Simulation simulation = mock(Simulation.class);
        final AnyOfFactory anyOfFactory = new AnyOfFactory(List.of());

        // Act
        final AnyOf result = anyOfFactory.newInstance(simulation);

        // Assert
        assertNotNull(result, "The result of newInstance should not be null");
        assertEquals(
            0,
            result.getPredicates().count(),
            "The resulting AnyOf should contain no predicates"
        );
    }

    @Test
    void testNewInstance_CallsFactoryGetMethod() {
        // Arrange
        final Simulation simulation = mock(Simulation.class);
        final Factory<Predicate<Object>> mockFactory = mock(Factory.class);
        when(mockFactory.get(simulation)).thenReturn(mock(Predicate.class));

        final AnyOfFactory anyOfFactory = new AnyOfFactory(List.of(mockFactory));

        // Act
        anyOfFactory.newInstance(simulation);

        // Assert
        verify(mockFactory, times(1)).get(simulation);
    }
}
