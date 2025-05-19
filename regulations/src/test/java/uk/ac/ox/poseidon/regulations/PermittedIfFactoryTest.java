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
import org.mockito.Mockito;
import uk.ac.ox.poseidon.agents.behaviours.Action;
import uk.ac.ox.poseidon.core.GlobalScopeFactory;
import uk.ac.ox.poseidon.core.Simulation;

import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

class PermittedIfFactoryTest {

    /**
     * Test class for {@link PermittedIfFactory}.
     * <p>
     * The {@code newInstance} method is responsible for creating and returning a new
     * {@link PermittedIf} instance using the provided {@link Simulation}. It utilizes an action
     * predicate from another {@link GlobalScopeFactory}.
     */

    @Test
    void testNewInstance_ShouldCreatePermittedIf() {
        // Mock the simulation and action predicate factory
        final Simulation simulation = Mockito.mock(Simulation.class);
        final GlobalScopeFactory<Predicate<Action>> mockedActionPredicateFactory = Mockito.mock(
            GlobalScopeFactory.class);

        // Mock the returned predicate
        final Predicate<Action> mockedPredicate = Mockito.mock(Predicate.class);
        when(mockedActionPredicateFactory.get(simulation)).thenReturn(mockedPredicate);

        // Create an instance of PermittedIfFactory with the mocked action predicate factory
        final PermittedIfFactory factory = new PermittedIfFactory(mockedActionPredicateFactory);

        // Call the newInstance method
        final PermittedIf permittedIf = factory.newInstance(simulation);

        // Verify output
        assertNotNull(permittedIf, "PermittedIf instance should not be null");
    }

    @Test
    void testNewInstance_WithDifferentPredicates_ShouldCreatePermittedIf() {
        // Mock the simulation and another action predicate factory
        final Simulation simulation = Mockito.mock(Simulation.class);
        final GlobalScopeFactory<Predicate<Action>> anotherMockedActionPredicateFactory =
            Mockito.mock(GlobalScopeFactory.class);

        // Mock a different predicate
        final Predicate<Action> anotherMockedPredicate = Mockito.mock(Predicate.class);
        when(anotherMockedActionPredicateFactory.get(simulation)).thenReturn(anotherMockedPredicate);

        // Create a new PermittedIfFactory instance with the different factory
        final PermittedIfFactory factory = new PermittedIfFactory(
            anotherMockedActionPredicateFactory);

        // Call the newInstance method
        final PermittedIf permittedIf = factory.newInstance(simulation);

        // Verify
        assertNotNull(permittedIf, "PermittedIf instance should not be null");
    }
}
