/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2026, University of Oxford.
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

package uk.ac.ox.poseidon.core;

import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.scopes.Scope;
import uk.ac.ox.poseidon.core.scopes.SimulationScope;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PerSimulationFactoryTest {

    private static SimulationScope scopeOfNewSimulation() {
        final Simulation simulation = mock(Simulation.class);
        final SimulationScope scope = mock(SimulationScope.class);
        when(scope.getSimulation()).thenReturn(simulation);
        return scope;
    }

    private static class NewObjectFactory extends GlobalScopeFactory<Object> {
        @Override
        protected Object newInstance(final Scope scope) {
            return new Object();
        }
    }

    @Test
    void sameSimulationGetsTheSameInstance() {
        final PerSimulationFactory<Object> factory =
            new PerSimulationFactory<>(new NewObjectFactory());
        final SimulationScope scope = scopeOfNewSimulation();

        assertThat(factory.get(scope)).isSameAs(factory.get(scope));
    }

    @Test
    void eachSimulationGetsItsOwnInstanceEvenFromASharedDelegate() {
        final PerSimulationFactory<Object> factory =
            new PerSimulationFactory<>(new NewObjectFactory());

        assertThat(factory.get(scopeOfNewSimulation()))
            .isNotSameAs(factory.get(scopeOfNewSimulation()));
    }

    @Test
    void theDelegatesOwnInstanceIsLeftAlone() {
        final NewObjectFactory delegate = new NewObjectFactory();
        final PerSimulationFactory<Object> factory = new PerSimulationFactory<>(delegate);
        final SimulationScope scope = scopeOfNewSimulation();

        assertThat(factory.get(scope)).isNotSameAs(delegate.get(scope));
    }
}
