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

import ec.util.MersenneTwisterFast;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.events.SimpleEventManager;
import uk.ac.ox.poseidon.fishing.trips.FishingTripEndEvent;
import uk.ac.ox.poseidon.fishing.trips.FishingTripStartEvent;
import uk.ac.ox.poseidon.fishing.trips.TripStartEvent;
import uk.ac.ox.poseidon.fishing.vessels.FishingTrip;
import uk.ac.ox.poseidon.fishing.vessels.Trip;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;

class FriendshipDynamicsTest {

  private final Vessel self = activeVessel();
  private final List<Vessel> candidates = new ArrayList<>(List.of(self));
  private final SocialNetwork network = new SocialNetwork(() -> candidates);
  private final MersenneTwisterFast rng = new MersenneTwisterFast(42);

  private static Vessel activeVessel() {
    final Vessel vessel = mock(Vessel.class);
    when(vessel.isActive()).thenReturn(true);
    return vessel;
  }

  private List<Vessel> addCandidates(final int count) {
    final List<Vessel> added = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      added.add(activeVessel());
    }
    candidates.addAll(added);
    return added;
  }

  private FriendshipDynamics dynamics(
    final Function<? super Vessel, ? extends Number> target,
    final BiFunction<? super Vessel, ? super Vessel, Boolean> condition
  ) {
    return new FriendshipDynamics(network, target, condition, rng);
  }

  private FriendshipDynamics dynamicsOfActiveFriends(final int target) {
    return dynamics(vessel -> target, (vessel, other) -> other.isActive());
  }

  private static FishingTrip tripOf(final Vessel vessel) {
    final FishingTrip trip = mock(FishingTrip.class);
    when(trip.getVessel()).thenReturn(vessel);
    return trip;
  }

  private static void startTrip(
    final FriendshipDynamics dynamics,
    final Vessel vessel
  ) {
    dynamics.receive(new FishingTripStartEvent(tripOf(vessel)));
  }

  @Test
  void listensForFishingTripStarts() {
    assertThat(dynamicsOfActiveFriends(1).getEventClass()).isEqualTo(
      FishingTripStartEvent.class
    );
  }

  @Test
  void drawsTiesFromTheCandidatesToTheStartingVesselUpToTheTarget() {
    addCandidates(5);

    startTrip(dynamicsOfActiveFriends(3), self);

    assertThat(network.getSources(self)).hasSize(3).doesNotContain(self);
    assertThat(network.getRecipients(self)).isEmpty();
  }

  @Test
  void drawsEveryCandidateWhenThereAreFewerThanTheTarget() {
    final List<Vessel> others = addCandidates(2);

    startTrip(dynamicsOfActiveFriends(3), self);

    assertThat(network.getSources(self)).containsExactlyElementsOf(others);
  }

  @Test
  void drawsOnlyCandidatesTheConditionRelatesToTheVessel() {
    final Vessel sameGroup = activeVessel();
    final Vessel otherGroup = activeVessel();
    candidates.addAll(List.of(sameGroup, otherGroup));

    startTrip(
      dynamics(
        vessel -> 3,
        (vessel, other) ->
          (vessel == self && other == sameGroup) ||
          (vessel != self && other == otherGroup)
      ),
      self
    );

    assertThat(network.getSources(self)).containsExactly(sameGroup);
  }

  @Test
  void leavesTheTiesAloneBetweenTripStartsOfAVesselWithEnoughFriends() {
    addCandidates(5);
    final FriendshipDynamics dynamics = dynamicsOfActiveFriends(3);
    startTrip(dynamics, self);
    final var first = network.getSources(self);

    startTrip(dynamics, self);

    assertThat(network.getSources(self)).isEqualTo(first);
  }

  @Test
  void countsATieMadeElsewhereAsAFriend() {
    final Vessel friend = addCandidates(1).getFirst();
    addCandidates(3);
    network.addTie(friend, self);

    startTrip(dynamicsOfActiveFriends(2), self);

    assertThat(network.getSources(self)).hasSize(2).contains(friend);
  }

  @Test
  void dropsASourceFailingTheCondition() {
    final Vessel friend = addCandidates(1).getFirst();
    final FriendshipDynamics dynamics = dynamicsOfActiveFriends(1);
    startTrip(dynamics, self);
    when(friend.isActive()).thenReturn(false);

    startTrip(dynamics, self);

    assertThat(network.getSources(self)).isEmpty();
  }

  @Test
  void replacesASourceFailingTheConditionWithANewTie() {
    final Vessel friend = addCandidates(1).getFirst();
    final FriendshipDynamics dynamics = dynamicsOfActiveFriends(1);
    startTrip(dynamics, self);
    when(friend.isActive()).thenReturn(false);
    final Vessel newcomer = addCandidates(1).getFirst();

    startTrip(dynamics, self);

    assertThat(network.getSources(self)).containsExactly(newcomer);
  }

  @Test
  void keepsExcessEligibleSourcesWhenTheTargetFalls() {
    addCandidates(5);
    final int[] target = { 4 };
    final FriendshipDynamics dynamics = dynamics(
      vessel -> target[0],
      (vessel, other) -> other.isActive()
    );
    startTrip(dynamics, self);
    final var four = network.getSources(self);

    target[0] = 1;
    startTrip(dynamics, self);

    assertThat(network.getSources(self)).isEqualTo(four);
  }

  @Test
  void recruitsAgainWhenTheTargetRisesAgain() {
    addCandidates(5);
    final int[] target = { 2 };
    final FriendshipDynamics dynamics = dynamics(
      vessel -> target[0],
      (vessel, other) -> other.isActive()
    );
    startTrip(dynamics, self);

    target[0] = 4;
    startTrip(dynamics, self);

    assertThat(network.getSources(self)).hasSize(4);
  }

  @Test
  void readsTheTargetOnceForTheStartingVesselOnly() {
    addCandidates(5);
    final List<Vessel> asked = new ArrayList<>();
    final AtomicInteger reads = new AtomicInteger();

    startTrip(
      dynamics(
        vessel -> {
          asked.add(vessel);
          reads.incrementAndGet();
          return 2;
        },
        (vessel, other) -> other.isActive()
      ),
      self
    );

    assertThat(reads).hasValue(1);
    assertThat(asked).containsExactly(self);
  }

  @Test
  void editsOnlyTheStartingVesselsIncomingTies() {
    final Vessel friend = addCandidates(1).getFirst();
    final Vessel other = addCandidates(1).getFirst();
    final Vessel another = addCandidates(1).getFirst();
    network.addTie(self, other);
    network.addTie(another, other);
    network.addTie(self, friend);

    startTrip(dynamicsOfActiveFriends(0), self);

    assertThat(network.getRecipients(self)).containsExactlyInAnyOrder(
      other,
      friend
    );
    assertThat(network.getSources(other)).containsExactlyInAnyOrder(
      self,
      another
    );
  }

  @Test
  void onlyAFishingTripStartTriggersRecruitment() {
    addCandidates(3);
    final SimpleEventManager eventManager = new SimpleEventManager();
    eventManager.addListener(dynamicsOfActiveFriends(2));
    final FishingTrip trip = tripOf(self);

    eventManager.broadcast(new FishingTripEndEvent(trip));
    assertThat(network.getSources(self)).isEmpty();

    eventManager.broadcast(new FishingTripStartEvent(trip));
    assertThat(network.getSources(self)).hasSize(2);
  }

  @Test
  void aTripHomeTriggersNoRecruitment() {
    addCandidates(3);
    final SimpleEventManager eventManager = new SimpleEventManager();
    eventManager.addListener(dynamicsOfActiveFriends(2));
    final Trip tripHome = mock(Trip.class);
    when(tripHome.getVessel()).thenReturn(self);

    eventManager.broadcast(new TripStartEvent(tripHome));

    assertThat(network.getSources(self)).isEmpty();
  }

  @Test
  void rejectsANegativeTarget() {
    addCandidates(1);

    assertThatThrownBy(() -> startTrip(dynamicsOfActiveFriends(-1), self))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("-1");
  }
}
