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

package uk.ac.ox.poseidon.regulations;

import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.agents.behaviours.Action;
import uk.ac.ox.poseidon.core.GlobalScopeFactory;
import uk.ac.ox.poseidon.core.Simulation;

import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class ForbiddenIfFactoryTest {

    /**
     * Tests the `newInstance` method of the `ForbiddenIfFactory` class.
     * <p>
     * The method `newInstance` is responsible for creating a new instance of the `ForbiddenIf`
     * class by invoking the `get` method on the action predicate factory with the provided
     * simulation.
     */

    @Test
    void testNewInstanceCreatesForbiddenIfObject() {
        // Arrange
        final Simulation mockSimulation = mock(Simulation.class);
        final Predicate<Action> mockActionPredicate = mock(Predicate.class);
        @SuppressWarnings("unchecked") final GlobalScopeFactory<Predicate<Action>>
            mockActionPredicateFactory =
            (GlobalScopeFactory<Predicate<Action>>) mock(GlobalScopeFactory.class);
        when(mockActionPredicateFactory.get(mockSimulation)).thenReturn(mockActionPredicate);

        final ForbiddenIfFactory forbiddenIfFactory =
            new ForbiddenIfFactory(mockActionPredicateFactory);

        // Act
        final ForbiddenIf result = forbiddenIfFactory.newInstance(mockSimulation);

        // Assert
        assertNotNull(result);
        verify(mockActionPredicateFactory, times(1)).get(mockSimulation);
    }
}
