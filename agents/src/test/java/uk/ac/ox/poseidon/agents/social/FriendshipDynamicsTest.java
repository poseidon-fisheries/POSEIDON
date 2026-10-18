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

import ec.util.MersenneTwisterFast;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.agents.vessels.Vessel;

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
    final int targetNumberOfFriends,
    final BiFunction<? super Vessel, ? super Vessel, Boolean> condition
  ) {
    return new FriendshipDynamics(
      network,
      targetNumberOfFriends,
      condition,
      rng
    );
  }

  private FriendshipDynamics dynamicsOfActiveFriends(final int target) {
    return dynamics(target, (vessel, other) -> other.isActive());
  }

  @Test
  void drawsTiesFromTheCandidatesToTheVesselUpToTheTarget() {
    addCandidates(5);

    final Set<Vessel> friends = dynamicsOfActiveFriends(3).apply(self);

    assertThat(friends).hasSize(3).doesNotContain(self);
    assertThat(network.getSources(self)).isEqualTo(friends);
    assertThat(network.getRecipients(self)).isEmpty();
  }

  @Test
  void drawsEveryCandidateWhenThereAreFewerThanTheTarget() {
    final List<Vessel> others = addCandidates(2);

    assertThat(
      dynamicsOfActiveFriends(3).apply(self)
    ).containsExactlyElementsOf(others);
  }

  @Test
  void drawsOnlyCandidatesTheConditionRelatesToTheVessel() {
    final Vessel sameGroup = activeVessel();
    final Vessel otherGroup = activeVessel();
    candidates.addAll(List.of(sameGroup, otherGroup));

    final Set<Vessel> friends = dynamics(
      3,
      (vessel, other) ->
        (vessel == self && other == sameGroup) ||
        (vessel != self && other == otherGroup)
    ).apply(self);

    assertThat(friends).containsExactly(sameGroup);
  }

  @Test
  void keepsTheVesselsFriendsFromOneReadToTheNext() {
    addCandidates(5);
    final FriendshipDynamics dynamics = dynamicsOfActiveFriends(3);

    final Set<Vessel> first = dynamics.apply(self);

    assertThat(dynamics.apply(self)).isEqualTo(first);
    assertThat(network.getSources(self)).isEqualTo(first);
  }

  @Test
  void countsATieMadeElsewhereAsAFriend() {
    final Vessel friend = addCandidates(1).getFirst();
    addCandidates(3);
    network.addTie(friend, self);

    final Set<Vessel> friends = dynamicsOfActiveFriends(2).apply(self);

    assertThat(friends).hasSize(2).contains(friend);
  }

  @Test
  void aFriendFailingTheConditionIsNotReadButKeepsItsTie() {
    final Vessel friend = addCandidates(1).getFirst();
    final FriendshipDynamics dynamics = dynamicsOfActiveFriends(1);
    dynamics.apply(self);

    when(friend.isActive()).thenReturn(false);

    assertThat(dynamics.apply(self)).isEmpty();
    assertThat(network.getSources(self)).containsExactly(friend);
  }

  @Test
  void aFriendPassingTheConditionAgainIsAFriendAgain() {
    final Vessel friend = addCandidates(1).getFirst();
    final FriendshipDynamics dynamics = dynamicsOfActiveFriends(1);
    dynamics.apply(self);
    when(friend.isActive()).thenReturn(false);
    dynamics.apply(self);

    when(friend.isActive()).thenReturn(true);

    assertThat(dynamics.apply(self)).containsExactly(friend);
  }

  @Test
  void replacesAFriendFailingTheConditionWithANewTie() {
    final Vessel friend = addCandidates(1).getFirst();
    final FriendshipDynamics dynamics = dynamicsOfActiveFriends(1);
    dynamics.apply(self);
    when(friend.isActive()).thenReturn(false);
    final Vessel newcomer = addCandidates(1).getFirst();

    assertThat(dynamics.apply(self)).containsExactly(newcomer);
    assertThat(network.getSources(self)).containsExactly(friend, newcomer);
  }

  @Test
  void readingTheNetworkDrawsNothing() {
    addCandidates(5);
    dynamicsOfActiveFriends(3);

    assertThat(network.getSources(self)).isEmpty();
  }

  @Test
  void givesAnUnmodifiableSet() {
    addCandidates(3);

    assertThatThrownBy(() ->
      dynamicsOfActiveFriends(2).apply(self).add(activeVessel())
    ).isInstanceOf(UnsupportedOperationException.class);
  }
}
