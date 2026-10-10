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
import uk.ac.ox.poseidon.core.events.Listener;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HaulSharerTest {

    private final Vessel a = mock(Vessel.class);
    private final Vessel b = mock(Vessel.class);
    private final SocialNetwork network = new SocialNetwork(() -> List.of(a, b));
    private final HaulSharer haulSharer = new HaulSharer(network);
    private final List<FishingEvent> heardByA = new ArrayList<>();
    private final List<FishingEvent> heardByB = new ArrayList<>();

    private static FishingEvent haulBy(final Vessel vessel) {
        final ExtendedFishingAction action = mock(ExtendedFishingAction.class);
        when(action.getAgent()).thenReturn(vessel);
        return new FishingEvent(action, null);
    }

    private static Listener<FishingEvent> listener(final List<FishingEvent> heard) {
        return new Listener<>() {
            @Override
            public Class<FishingEvent> getEventClass() {
                return FishingEvent.class;
            }

            @Override
            public void receive(final FishingEvent event) {
                heard.add(event);
            }
        };
    }

    @Test
    void aHaulReachesTheSendersRecipientsOnly() {
        haulSharer.registerSharedHaulListener(a, listener(heardByA));
        haulSharer.registerSharedHaulListener(b, listener(heardByB));
        network.addTie(a, b);
        final FishingEvent haulByA = haulBy(a);

        haulSharer.receive(haulByA);
        haulSharer.receive(haulBy(b));

        assertThat(heardByB).containsExactly(haulByA);
        assertThat(heardByA).isEmpty();
    }

    @Test
    void aHaulIsNotDeliveredBackToItsSender() {
        haulSharer.registerSharedHaulListener(a, listener(heardByA));
        haulSharer.registerSharedHaulListener(b, listener(heardByB));
        network.addTie(a, b);
        network.addTie(b, a);

        haulSharer.receive(haulBy(a));

        assertThat(heardByA).isEmpty();
        assertThat(heardByB).hasSize(1);
    }

    @Test
    void skipsARecipientWithNoSharedHaulListener() {
        network.addTie(a, b);

        haulSharer.receive(haulBy(a));
    }

    @Test
    void deliversNothingWithoutTies() {
        haulSharer.registerSharedHaulListener(a, listener(heardByA));
        haulSharer.registerSharedHaulListener(b, listener(heardByB));

        haulSharer.receive(haulBy(a));

        assertThat(heardByA).isEmpty();
        assertThat(heardByB).isEmpty();
    }

    @Test
    void rejectsASecondSharedHaulListenerForTheSameVessel() {
        haulSharer.registerSharedHaulListener(a, listener(heardByA));

        assertThatThrownBy(() -> haulSharer.registerSharedHaulListener(a, listener(heardByB)))
            .isInstanceOf(IllegalStateException.class);
    }
}
