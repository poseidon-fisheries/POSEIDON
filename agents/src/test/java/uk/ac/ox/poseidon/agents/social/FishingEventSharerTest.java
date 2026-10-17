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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.agents.regulations.actions.ExtendedFishingAction;
import uk.ac.ox.poseidon.agents.tasks.fishing.FishingEvent;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.core.events.Listener;

class FishingEventSharerTest {

  private final Vessel a = mock(Vessel.class);
  private final Vessel b = mock(Vessel.class);
  private final SocialNetwork network = new SocialNetwork(() -> List.of(a, b));
  private final FishingEventSharer fishingEventSharer = new FishingEventSharer(
    network
  );
  private final List<FishingEvent> heardByA = new ArrayList<>();
  private final List<FishingEvent> heardByB = new ArrayList<>();

  private static FishingEvent fishingEventBy(final Vessel vessel) {
    final ExtendedFishingAction action = mock(ExtendedFishingAction.class);
    when(action.getAgent()).thenReturn(vessel);
    return new FishingEvent(action, null);
  }

  private static Listener<FishingEvent> listener(
    final List<FishingEvent> heard
  ) {
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
  void aFishingEventReachesTheSendersRecipientsOnly() {
    fishingEventSharer.register(a, listener(heardByA));
    fishingEventSharer.register(b, listener(heardByB));
    network.addTie(a, b);
    final FishingEvent fishingEventByA = fishingEventBy(a);

    fishingEventSharer.receive(fishingEventByA);
    fishingEventSharer.receive(fishingEventBy(b));

    assertThat(heardByB).containsExactly(fishingEventByA);
    assertThat(heardByA).isEmpty();
  }

  @Test
  void aFishingEventIsNotDeliveredBackToItsSender() {
    fishingEventSharer.register(a, listener(heardByA));
    fishingEventSharer.register(b, listener(heardByB));
    network.addTie(a, b);
    network.addTie(b, a);

    fishingEventSharer.receive(fishingEventBy(a));

    assertThat(heardByA).isEmpty();
    assertThat(heardByB).hasSize(1);
  }

  @Test
  void skipsARecipientWithNoSharedFishingEventListener() {
    network.addTie(a, b);

    fishingEventSharer.receive(fishingEventBy(a));
  }

  @Test
  void deliversNothingWithoutTies() {
    fishingEventSharer.register(a, listener(heardByA));
    fishingEventSharer.register(b, listener(heardByB));

    fishingEventSharer.receive(fishingEventBy(a));

    assertThat(heardByA).isEmpty();
    assertThat(heardByB).isEmpty();
  }

  @Test
  void rejectsASecondSharedFishingEventListenerForTheSameVessel() {
    fishingEventSharer.register(a, listener(heardByA));

    assertThatThrownBy(() ->
      fishingEventSharer.register(a, listener(heardByB))
    ).isInstanceOf(IllegalStateException.class);
  }
}
