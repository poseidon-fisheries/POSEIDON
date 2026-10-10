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
import uk.ac.ox.poseidon.agents.vessels.VesselScope;
import uk.ac.ox.poseidon.agents.vessels.VesselScopeFactory;
import uk.ac.ox.poseidon.agents.vessels.VesselsGetter;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.events.AbstractListener;
import uk.ac.ox.poseidon.core.events.SimpleEventManager;
import uk.ac.ox.poseidon.core.scopes.SimulationScope;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.ac.ox.poseidon.agents.social.Factories.haulSharer;
import static uk.ac.ox.poseidon.agents.social.Factories.sharedHaulListener;
import static uk.ac.ox.poseidon.agents.social.Factories.socialNetwork;
import static uk.ac.ox.poseidon.core.utils.Factories.object;

class SharedHaulListenerFactoryTest {

    private final Simulation simulation = simulation();
    private final Vessel a = mock(Vessel.class);
    private final Vessel b = mock(Vessel.class);
    private final VesselsGetter fleet = () -> List.of(a, b);
    private final SimulationScope simulationScope = new SimulationScope(simulation);
    private final VesselScope scopeB = new VesselScope(simulationScope, b);
    private final SocialNetworkFactory network = socialNetwork(object(fleet));
    private final HaulSharerFactory haulSharer = haulSharer(network);
    private final RecordingListenerFactory listener = new RecordingListenerFactory();
    private final SharedHaulListenerFactory<RecordingListener> factory =
        sharedHaulListener(listener, haulSharer);

    private static Simulation simulation() {
        final Simulation simulation = mock(Simulation.class);
        when(simulation.getEventManager()).thenReturn(new SimpleEventManager());
        return simulation;
    }

    private static class RecordingListener extends AbstractListener<FishingEvent> {
        private final List<FishingEvent> heard = new ArrayList<>();

        RecordingListener() {
            super(FishingEvent.class);
        }

        @Override
        public void receive(final FishingEvent event) {
            heard.add(event);
        }
    }

    private static class RecordingListenerFactory extends VesselScopeFactory<RecordingListener> {
        @Override
        protected RecordingListener newInstance(final VesselScope scope) {
            return new RecordingListener();
        }
    }

    private static FishingEvent haulBy(final Vessel vessel) {
        final ExtendedFishingAction action = mock(ExtendedFishingAction.class);
        when(action.getAgent()).thenReturn(vessel);
        return new FishingEvent(action, null);
    }

    @Test
    void aHaulByOneOfTheVesselsSourcesReachesItsListener() {
        factory.get(scopeB);
        network.get(simulationScope).addTie(a, b);
        final FishingEvent haulByA = haulBy(a);

        haulSharer.get(simulationScope).receive(haulByA);

        assertThat(listener.get(scopeB).heard).containsExactly(haulByA);
    }

    @Test
    void givesTheListenerTheListenersFactoryGivesForTheVessel() {
        assertThat(factory.get(scopeB)).isNotNull().isSameAs(listener.get(scopeB));
    }

    @Test
    void buildingItTwiceForAVesselRegistersOnce() {
        factory.get(scopeB);
        factory.get(scopeB);
        network.get(simulationScope).addTie(a, b);

        haulSharer.get(simulationScope).receive(haulBy(a));

        assertThat(listener.get(scopeB).heard).hasSize(1);
    }
}
