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

import ec.util.MersenneTwisterFast;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.agents.trips.Trip;
import uk.ac.ox.poseidon.agents.trips.TripStartEvent;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.agents.vessels.VesselsGetter;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.events.EventManager;
import uk.ac.ox.poseidon.core.events.ForwardingEventManager;
import uk.ac.ox.poseidon.core.events.SimpleEventManager;
import uk.ac.ox.poseidon.core.scopes.SimulationScope;

import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.ac.ox.poseidon.agents.social.Factories.cliqueDynamics;
import static uk.ac.ox.poseidon.agents.social.Factories.socialNetwork;
import static uk.ac.ox.poseidon.core.utils.Factories.object;

class CliqueDynamicsFactoryTest {

    private final EventManager simulationEventManager = new SimpleEventManager();
    private final Simulation simulation = simulation(simulationEventManager);
    private final Simulation otherSimulation = simulation(new SimpleEventManager());
    private final Vessel a = vessel(simulationEventManager);
    private final Vessel b = vessel(simulationEventManager);
    private final VesselsGetter fleet = () -> List.of(a, b);
    private final Function<Vessel, String> groupingKey = vessel -> "X";
    private final SocialNetworkFactory network = socialNetwork(object(fleet));
    private final CliqueDynamicsFactory factory =
        cliqueDynamics(network, object(groupingKey), 5);

    private static Simulation simulation(final EventManager eventManager) {
        final Simulation simulation = mock(Simulation.class);
        simulation.random = new MersenneTwisterFast(0);
        when(simulation.getEventManager()).thenReturn(eventManager);
        return simulation;
    }

    private static Vessel vessel(final EventManager simulationEventManager) {
        final Vessel vessel = mock(Vessel.class);
        when(vessel.isActive()).thenReturn(true);
        when(vessel.getEventManager())
            .thenReturn(new ForwardingEventManager(simulationEventManager));
        return vessel;
    }

    @Test
    void givesOneDynamicsPerSimulation() {
        final SimulationScope scope = new SimulationScope(simulation);
        final SimulationScope otherScope = new SimulationScope(otherSimulation);

        assertThat(factory.get(scope)).isNotNull().isSameAs(factory.get(scope));
        assertThat(factory.get(scope)).isNotSameAs(factory.get(otherScope));
    }

    @Test
    void aTripStartBroadcastOnAVesselsEventManagerFormsACliqueThroughForwarding() {
        final SimulationScope scope = new SimulationScope(simulation);
        factory.get(scope);
        final Trip trip = mock(Trip.class);
        when(trip.getVessel()).thenReturn(a);

        a.getEventManager().broadcast(new TripStartEvent(trip));

        assertThat(network.get(scope).getRecipients(a)).containsExactly(b);
        assertThat(network.get(scope).getRecipients(b)).containsExactly(a);
    }
}
