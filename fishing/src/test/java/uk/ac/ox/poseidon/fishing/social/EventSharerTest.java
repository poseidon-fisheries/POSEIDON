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

package uk.ac.ox.poseidon.fishing.social;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.events.AbstractListener;
import uk.ac.ox.poseidon.core.events.Listener;
import uk.ac.ox.poseidon.fishing.regulations.actions.ExtendedFishingAction;
import uk.ac.ox.poseidon.fishing.tasks.fishing.FishingActionEvent;
import uk.ac.ox.poseidon.fishing.trips.FishingTripEndEvent;
import uk.ac.ox.poseidon.fishing.vessels.FishingTrip;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;

class EventSharerTest {

  private final Vessel a = mock(Vessel.class);
  private final Vessel b = mock(Vessel.class);
  private final SocialNetwork network = new SocialNetwork(() -> List.of(a, b));
  private final EventSharer<FishingActionEvent> fishingActionSharer =
    new EventSharer<>(FishingActionEvent.class, network);
  private final List<FishingActionEvent> heardByA = new ArrayList<>();
  private final List<FishingActionEvent> heardByB = new ArrayList<>();

  private static FishingActionEvent fishingEventBy(final Vessel vessel) {
    final ExtendedFishingAction action = mock(ExtendedFishingAction.class);
    when(action.getAgent()).thenReturn(vessel);
    return new FishingActionEvent(action, null);
  }

  private static Listener<FishingActionEvent> listener(
    final List<FishingActionEvent> heard
  ) {
    return new Listener<>() {
      @Override
      public Class<FishingActionEvent> getEventClass() {
        return FishingActionEvent.class;
      }

      @Override
      public void receive(final FishingActionEvent event) {
        heard.add(event);
      }
    };
  }

  @Test
  void aFishingEventReachesTheSendersRecipientsOnly() {
    fishingActionSharer.register(a, listener(heardByA));
    fishingActionSharer.register(b, listener(heardByB));
    network.addTie(a, b);
    final FishingActionEvent fishingEventByA = fishingEventBy(a);

    fishingActionSharer.receive(fishingEventByA);
    fishingActionSharer.receive(fishingEventBy(b));

    assertThat(heardByB).containsExactly(fishingEventByA);
    assertThat(heardByA).isEmpty();
  }

  @Test
  void aFishingEventIsNotDeliveredBackToItsSender() {
    fishingActionSharer.register(a, listener(heardByA));
    fishingActionSharer.register(b, listener(heardByB));
    network.addTie(a, b);
    network.addTie(b, a);

    fishingActionSharer.receive(fishingEventBy(a));

    assertThat(heardByA).isEmpty();
    assertThat(heardByB).hasSize(1);
  }

  @Test
  void skipsARecipientWithNoSharedFishingEventListener() {
    network.addTie(a, b);

    fishingActionSharer.receive(fishingEventBy(a));
  }

  @Test
  void deliversNothingWithoutTies() {
    fishingActionSharer.register(a, listener(heardByA));
    fishingActionSharer.register(b, listener(heardByB));

    fishingActionSharer.receive(fishingEventBy(a));

    assertThat(heardByA).isEmpty();
    assertThat(heardByB).isEmpty();
  }

  @Test
  void rejectsASecondSharedFishingEventListenerForTheSameVessel() {
    fishingActionSharer.register(a, listener(heardByA));

    assertThatThrownBy(() ->
      fishingActionSharer.register(a, listener(heardByB))
    ).isInstanceOf(IllegalStateException.class);
  }

  @Test
  void aFishingTripEndReachesTheSendersRecipientsIntact() {
    final EventSharer<FishingTripEndEvent> tripEndSharer = new EventSharer<>(
      FishingTripEndEvent.class,
      network
    );
    final List<FishingTripEndEvent> heardByB = new ArrayList<>();
    tripEndSharer.register(
      b,
      new AbstractListener<>(FishingTripEndEvent.class) {
        @Override
        public void receive(final FishingTripEndEvent event) {
          heardByB.add(event);
        }
      }
    );
    network.addTie(a, b);
    final FishingTrip trip = mock(FishingTrip.class);
    when(trip.getVessel()).thenReturn(a);
    final FishingTripEndEvent event = new FishingTripEndEvent(trip);

    tripEndSharer.receive(event);

    assertThat(heardByB).containsExactly(event);
  }

  @Test
  void aTieMadeAfterAnEventGivesNoHistory() {
    fishingActionSharer.register(b, listener(heardByB));
    fishingActionSharer.receive(fishingEventBy(a));

    network.addTie(a, b);

    assertThat(heardByB).isEmpty();
  }

  @Test
  void aDroppedTieStopsFurtherDeliveryAndTakesNothingBack() {
    fishingActionSharer.register(b, listener(heardByB));
    network.addTie(a, b);
    fishingActionSharer.receive(fishingEventBy(a));

    network.removeTie(a, b);
    fishingActionSharer.receive(fishingEventBy(a));

    assertThat(heardByB).hasSize(1);
  }
}
