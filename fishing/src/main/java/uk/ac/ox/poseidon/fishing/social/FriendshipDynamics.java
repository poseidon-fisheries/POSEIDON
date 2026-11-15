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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static uk.ac.ox.poseidon.core.MasonUtils.reservoirSample;

import ec.util.MersenneTwisterFast;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import uk.ac.ox.poseidon.core.events.AbstractListener;
import uk.ac.ox.poseidon.fishing.trips.FishingTripStartEvent;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;

/**
 * Forms and drops the friends of a vessel, the sources of its incoming ties,
 * when it starts a fishing trip. It edits no other tie: a trip home starts
 * nothing, and the ties out of the vessel are left to those it tells.
 */
public class FriendshipDynamics
  extends AbstractListener<FishingTripStartEvent>
{

  private final SocialNetwork network;
  private final Function<
    ? super Vessel,
    ? extends Number
  > targetNumberOfFriends;
  private final BiFunction<
    ? super Vessel,
    ? super Vessel,
    Boolean
  > friendshipCondition;
  private final MersenneTwisterFast rng;

  FriendshipDynamics(
    final SocialNetwork network,
    final Function<? super Vessel, ? extends Number> targetNumberOfFriends,
    final BiFunction<
      ? super Vessel,
      ? super Vessel,
      Boolean
    > friendshipCondition,
    final MersenneTwisterFast rng
  ) {
    super(FishingTripStartEvent.class);
    this.network = checkNotNull(network);
    this.targetNumberOfFriends = checkNotNull(targetNumberOfFriends);
    this.friendshipCondition = checkNotNull(friendshipCondition);
    this.rng = checkNotNull(rng);
  }

  /**
   * Drops the ties from sources the condition no longer relates to the
   * starting vessel. If it then has fewer friends than its target, read once
   * here, draws new ties to it, uniformly and without replacement, among the
   * candidates the condition relates to it. Friends beyond the target are
   * kept: the target governs recruitment, not a maximum.
   */
  @Override
  public void receive(final FishingTripStartEvent event) {
    final Vessel vessel = event.getVessel();
    final int target = targetNumberOfFriends.apply(vessel).intValue();
    checkArgument(
      target >= 0,
      "The target number of friends must not be negative, not %s.",
      target
    );
    final Set<Vessel> friends = new HashSet<>();
    for (final Vessel source : network.getSources(vessel)) {
      if (friendshipCondition.apply(vessel, source)) {
        friends.add(source);
      } else {
        network.removeTie(source, vessel);
      }
    }
    if (friends.size() < target) {
      reservoirSample(
        network.getCandidates(),
        target - friends.size(),
        candidate ->
          candidate != vessel &&
          !friends.contains(candidate) &&
          friendshipCondition.apply(vessel, candidate),
        rng
      ).forEach(friend -> network.addTie(friend, vessel));
    }
  }
}
