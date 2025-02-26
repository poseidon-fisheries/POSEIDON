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

package uk.ac.ox.poseidon.core.predicates.logical;

import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.Simulation;

import java.util.List;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class AllOfFactoryTest {

    /**
     * Tests for the AllOfFactory's newInstance method. AllOfFactory is responsible for creating
     * instances of the AllOf class, which combines multiple predicates into an "all-of" logical
     * combination.
     */

    @Test
    void newInstance_createsInstanceWithAllPredicates() {
        // Arrange
        final Simulation simulationMock = mock(Simulation.class);
        final Predicate<Object> predicate1 = mock(Predicate.class);
        final Predicate<Object> predicate2 = mock(Predicate.class);

        final Factory<Predicate<Object>> factoryMock1 = mock(Factory.class);
        final Factory<Predicate<Object>> factoryMock2 = mock(Factory.class);

        when(factoryMock1.get(simulationMock)).thenReturn(predicate1);
        when(factoryMock2.get(simulationMock)).thenReturn(predicate2);

        final AllOfFactory allOfFactory = new AllOfFactory(List.of(factoryMock1, factoryMock2));

        // Act
        final AllOf allOf = allOfFactory.newInstance(simulationMock);

        // Assert
        assertEquals(
            2,
            allOf.getPredicates().count(),
            "The AllOf instance should contain two predicates"
        );
        verify(factoryMock1, times(1)).get(simulationMock);
        verify(factoryMock2, times(1)).get(simulationMock);
    }

    @Test
    void newInstance_throwsExceptionWhenSimulationIsNull() {
        // Arrange
        final Factory<Predicate<Object>> factoryMock = mock(Factory.class);
        final AllOfFactory allOfFactory = new AllOfFactory(List.of(factoryMock));

        // Act & Assert
        assertThrows(
            NullPointerException.class, () -> allOfFactory.newInstance(null),
            "A NullPointerException should be thrown if simulation is null"
        );
    }

    @Test
    void newInstance_createsInstanceWithEmptyPredicateList() {
        // Arrange
        final Simulation simulationMock = mock(Simulation.class);
        final AllOfFactory allOfFactory = new AllOfFactory(List.of());

        // Act
        final AllOf allOf = allOfFactory.newInstance(simulationMock);

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
        final Simulation simulationMock = mock(Simulation.class);
        final Factory<Predicate<Object>> factoryMock = mock(Factory.class);
        final Predicate<Object> predicateMock = mock(Predicate.class);

        when(factoryMock.get(simulationMock)).thenReturn(predicateMock);

        final AllOfFactory allOfFactory = new AllOfFactory(List.of(factoryMock));

        // Act
        allOfFactory.newInstance(simulationMock);

        // Assert
        verify(factoryMock, times(1)).get(simulationMock);
    }
}
