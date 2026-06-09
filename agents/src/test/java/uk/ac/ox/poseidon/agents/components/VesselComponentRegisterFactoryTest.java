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

package uk.ac.ox.poseidon.agents.components;

import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.scopes.SimulationScope;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class VesselComponentRegisterFactoryTest {

    @Test
    void returnsSameRegisterForSameSimulation() {
        final VesselComponentRegisterFactory<String> factory =
            new VesselComponentRegisterFactory<>();
        final Simulation simulation = mock(Simulation.class);
        final SimulationScope scope = new SimulationScope(simulation);

        final VesselComponentRegister<String> first = factory.get(scope);
        final VesselComponentRegister<String> second = factory.get(scope);

        assertThat(first).isSameAs(second);
    }

    @Test
    void returnsDifferentRegistersForDifferentSimulations() {
        final VesselComponentRegisterFactory<String> factory =
            new VesselComponentRegisterFactory<>();
        final SimulationScope firstScope = new SimulationScope(mock(Simulation.class));
        final SimulationScope secondScope = new SimulationScope(mock(Simulation.class));

        final VesselComponentRegister<String> first = factory.get(firstScope);
        final VesselComponentRegister<String> second = factory.get(secondScope);

        assertThat(first).isNotSameAs(second);
    }
}
