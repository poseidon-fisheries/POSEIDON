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
package uk.ac.ox.poseidon.agents.social;

import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.agents.fields.VesselField;
import uk.ac.ox.poseidon.agents.market.MarketGrid;
import uk.ac.ox.poseidon.agents.vessels.Fleet;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.events.EventManager;
import uk.ac.ox.poseidon.core.schedule.TemporalSchedule;
import uk.ac.ox.poseidon.core.scopes.SimulationScope;
import uk.ac.ox.poseidon.geography.ports.PortGrid;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static uk.ac.ox.poseidon.agents.social.Factories.socialNetwork;

class SocialNetworkFactoryTest {

    private final Simulation simulation = mock(Simulation.class);
    private final Simulation otherSimulation = mock(Simulation.class);
    private final Fleet fleet = new Fleet(
        mock(TemporalSchedule.class),
        mock(EventManager.class),
        mock(VesselField.class),
        mock(PortGrid.class),
        mock(MarketGrid.class)
    );
    private final SocialNetworkFactory factory = socialNetwork(fleetFactory());

    private Factory<SimulationScope, Fleet> fleetFactory() {
        return scope -> fleet;
    }

    @Test
    void givesOneNetworkPerSimulation() {
        final SimulationScope scope = new SimulationScope(simulation);
        final SimulationScope otherScope = new SimulationScope(otherSimulation);

        assertThat(factory.get(scope)).isNotNull().isSameAs(factory.get(scope));
        assertThat(factory.get(scope)).isNotSameAs(factory.get(otherScope));
    }

    @Test
    void vesselsAddedToTheFleetLaterAreCandidates() {
        final SocialNetwork network = factory.get(new SimulationScope(simulation));

        final Vessel vessel = fleet.createVessel("A");

        assertThat(network.getCandidates()).containsExactly(vessel);
    }
}
