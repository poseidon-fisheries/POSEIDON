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

package uk.ac.ox.poseidon.agents.vessels;

import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.events.AbstractListener;
import uk.ac.ox.poseidon.core.events.EventManager;
import uk.ac.ox.poseidon.core.events.SimpleEventManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.ac.ox.poseidon.agents.vessels.Factories.vesselEventListener;

class VesselEventListenerFactoryTest {

    private final VesselScope scopeA = scopeOfNewVessel();
    private final VesselScope scopeB = scopeOfNewVessel();
    private final CountingListenerFactory listener = new CountingListenerFactory();
    private final VesselEventListenerFactory<CountingListener> factory =
        vesselEventListener(listener);

    private static VesselScope scopeOfNewVessel() {
        final EventManager eventManager = new SimpleEventManager();
        final Vessel vessel = mock(Vessel.class);
        when(vessel.getEventManager()).thenReturn(eventManager);
        final VesselScope scope = mock(VesselScope.class);
        when(scope.getVessel()).thenReturn(vessel);
        return scope;
    }

    private static class CountingListener extends AbstractListener<String> {
        private int count = 0;

        CountingListener() {
            super(String.class);
        }

        @Override
        public void receive(final String event) {
            count++;
        }
    }

    private static class CountingListenerFactory extends VesselScopeFactory<CountingListener> {
        @Override
        protected CountingListener newInstance(final VesselScope scope) {
            return new CountingListener();
        }
    }

    private static void broadcastOnVessel(final VesselScope scope) {
        scope.getVessel().getEventManager().broadcast("event");
    }

    @Test
    void registersTheListenerOnTheVesselsEventManager() {
        factory.get(scopeA);
        broadcastOnVessel(scopeA);

        assertThat(listener.get(scopeA).count).isEqualTo(1);
    }

    @Test
    void registersTheListenerOnlyOnceForAVessel() {
        factory.get(scopeA);
        factory.get(scopeA);
        broadcastOnVessel(scopeA);

        assertThat(listener.get(scopeA).count).isEqualTo(1);
    }

    @Test
    void registersEachVesselsListenerOnItsOwnEventManager() {
        factory.get(scopeA);
        factory.get(scopeB);
        broadcastOnVessel(scopeB);

        assertThat(listener.get(scopeA).count).isZero();
        assertThat(listener.get(scopeB).count).isEqualTo(1);
    }

    @Test
    void givesTheListenerTheListenersFactoryGivesForTheVessel() {
        assertThat(factory.get(scopeA)).isNotNull().isSameAs(listener.get(scopeA));
    }
}
