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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static uk.ac.ox.poseidon.core.MasonUtils.reservoirSample;

import ec.util.MersenneTwisterFast;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import uk.ac.ox.poseidon.agents.vessels.Vessel;

/**
 * Network dynamics that give each vessel friends: a friend of a vessel is a
 * vessel that tells it, through a tie from the friend to the vessel, and that
 * the friendship condition relates to the vessel when its friends are read.
 * Reading a vessel's friends through these dynamics first draws new ties, from
 * the network's candidates the condition relates to the vessel, until it has
 * the target number of friends, if there are enough candidates. A tie to a
 * vessel that fails the condition is kept, but not read, so that a vessel
 * that passes the condition again, such as one that moves away and back, is a
 * friend again. Only reading friends through these dynamics draws: reading the
 * network itself never does.
 */
public class FriendshipDynamics implements Function<Vessel, Set<Vessel>> {

  private final SocialNetwork network;
  private final int targetNumberOfFriends;
  private final BiFunction<
    ? super Vessel,
    ? super Vessel,
    Boolean
  > friendshipCondition;
  private final MersenneTwisterFast rng;

  FriendshipDynamics(
    final SocialNetwork network,
    final int targetNumberOfFriends,
    final BiFunction<
      ? super Vessel,
      ? super Vessel,
      Boolean
    > friendshipCondition,
    final MersenneTwisterFast rng
  ) {
    checkArgument(
      targetNumberOfFriends >= 0,
      "The target number of friends must not be negative, not %s.",
      targetNumberOfFriends
    );
    this.network = checkNotNull(network);
    this.targetNumberOfFriends = targetNumberOfFriends;
    this.friendshipCondition = checkNotNull(friendshipCondition);
    this.rng = checkNotNull(rng);
  }

  /**
   * Draws new ties to {@code vessel} if it has fewer friends than the target,
   * sampling uniformly, without replacement, among the candidates the
   * condition relates to it that do not already tell it.
   *
   * @param vessel the vessel whose friends to read
   * @return the vessels that tell {@code vessel} and that the condition
   *         relates to it, in the order their ties were made, unmodifiable
   */
  @Override
  public Set<Vessel> apply(final Vessel vessel) {
    final Set<Vessel> sources = network.getSources(vessel);
    final Set<Vessel> friends = new LinkedHashSet<>();
    for (final Vessel source : sources) {
      if (friendshipCondition.apply(vessel, source)) {
        friends.add(source);
      }
    }
    if (friends.size() < targetNumberOfFriends) {
      reservoirSample(
        network.getCandidates(),
        targetNumberOfFriends - friends.size(),
        candidate ->
          candidate != vessel &&
          !sources.contains(candidate) &&
          friendshipCondition.apply(vessel, candidate),
        rng
      ).forEach(friend -> {
        network.addTie(friend, vessel);
        friends.add(friend);
      });
    }
    return Collections.unmodifiableSet(friends);
  }
}
