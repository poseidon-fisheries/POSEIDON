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

package uk.ac.ox.poseidon.core.events;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

class ForwardingEventManagerTest {

    @Test
    void broadcastForwardsToBothPrimaryAndSecondaryManagers() {
        final EventManager secondary = mock(EventManager.class);
        final ForwardingEventManager forwarding = new ForwardingEventManager(secondary);

        final List<String> received = new ArrayList<>();
        forwarding.addListener(new SimpleListener<>(String.class, received::add));

        forwarding.broadcast("event");

        verify(secondary).broadcast("event");
        org.junit.jupiter.api.Assertions.assertEquals(1, received.size());
        org.junit.jupiter.api.Assertions.assertEquals("event", received.getFirst());
    }

    @Test
    void addAndRemoveAffectOnlyPrimaryManager() {
        final EventManager secondary = mock(EventManager.class);
        final ForwardingEventManager forwarding = new ForwardingEventManager(secondary);

        final List<String> received = new ArrayList<>();
        final Listener<String> listener = new SimpleListener<>(String.class, received::add);
        forwarding.addListener(listener);
        forwarding.removeListener(listener);

        forwarding.broadcast("event");

        verify(secondary).broadcast("event");
        org.junit.jupiter.api.Assertions.assertTrue(received.isEmpty());
        verifyNoMoreInteractions(secondary);
    }
}
