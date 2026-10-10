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
import uk.ac.ox.poseidon.agents.regulations.actions.ExtendedFishingAction;
import uk.ac.ox.poseidon.agents.tasks.fishing.FishingEvent;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.agents.vessels.VesselsGetter;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.events.EventManager;
import uk.ac.ox.poseidon.core.events.ForwardingEventManager;
import uk.ac.ox.poseidon.core.events.Listener;
import uk.ac.ox.poseidon.core.events.SimpleEventManager;
import uk.ac.ox.poseidon.core.scopes.SimulationScope;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.ac.ox.poseidon.agents.social.Factories.haulSharer;
import static uk.ac.ox.poseidon.agents.social.Factories.socialNetwork;
import static uk.ac.ox.poseidon.core.utils.Factories.object;

class HaulSharerFactoryTest {

    private final EventManager simulationEventManager = new SimpleEventManager();
    private final Simulation simulation = simulation(simulationEventManager);
    private final Simulation otherSimulation = simulation(new SimpleEventManager());
    private final Vessel a = vessel(simulationEventManager);
    private final Vessel b = vessel(simulationEventManager);
    private final VesselsGetter fleet = () -> List.of(a, b);
    private final SocialNetworkFactory network = socialNetwork(object(fleet));
    private final HaulSharerFactory factory = haulSharer(network);

    private static Simulation simulation(final EventManager eventManager) {
        final Simulation simulation = mock(Simulation.class);
        when(simulation.getEventManager()).thenReturn(eventManager);
        return simulation;
    }

    private static Vessel vessel(final EventManager simulationEventManager) {
        final Vessel vessel = mock(Vessel.class);
        when(vessel.getEventManager())
            .thenReturn(new ForwardingEventManager(simulationEventManager));
        return vessel;
    }

    @Test
    void givesOneSharerPerSimulation() {
        final SimulationScope scope = new SimulationScope(simulation);
        final SimulationScope otherScope = new SimulationScope(otherSimulation);

        assertThat(factory.get(scope)).isNotNull().isSameAs(factory.get(scope));
        assertThat(factory.get(scope)).isNotSameAs(factory.get(otherScope));
    }

    @Test
    void aHaulBroadcastOnAVesselsEventManagerReachesItsRecipientThroughForwarding() {
        final SimulationScope scope = new SimulationScope(simulation);
        final List<FishingEvent> heardByB = new ArrayList<>();
        factory.get(scope).registerSharedHaulListener(b, new Listener<FishingEvent>() {
            @Override
            public Class<FishingEvent> getEventClass() {
                return FishingEvent.class;
            }

            @Override
            public void receive(final FishingEvent event) {
                heardByB.add(event);
            }
        });
        network.get(scope).addTie(a, b);
        final ExtendedFishingAction action = mock(ExtendedFishingAction.class);
        when(action.getAgent()).thenReturn(a);
        final FishingEvent haulByA = new FishingEvent(action, null);

        a.getEventManager().broadcast(haulByA);

        assertThat(heardByB).containsExactly(haulByA);
    }
}
