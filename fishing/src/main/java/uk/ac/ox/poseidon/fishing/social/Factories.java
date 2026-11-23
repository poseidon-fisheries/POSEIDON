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

import static uk.ac.ox.poseidon.core.providers.constant.Factories.constant;
import static uk.ac.ox.poseidon.core.providers.random.Factories.rng;

import com.google.common.collect.ImmutableList;
import java.util.List;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.MappingFactory;
import uk.ac.ox.poseidon.core.SimulationScope;
import uk.ac.ox.poseidon.core.ValueFactory;
import uk.ac.ox.poseidon.core.events.Listener;
import uk.ac.ox.poseidon.fishing.tasks.fishing.FishingActionEvent;
import uk.ac.ox.poseidon.fishing.trips.FishingTripEndEvent;
import uk.ac.ox.poseidon.fishing.vessels.Fleet;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;
import uk.ac.ox.poseidon.fishing.vessels.VesselComponentScope;

/** Factories for the ties between vessels. */
public final class Factories {

  private Factories() {}

  /**
   * @param fleet the fleet whose vessels ties can be made with; an exit from
   *              it removes every tie in and out of the vessel
   * @return a {@link uk.ac.ox.poseidon.core.SimulationScopeFactory} for a
   *         {@link SocialNetwork} with no ties
   * @see SocialNetwork
   */
  public static SocialNetworkFactory socialNetwork(
    final Factory<? super SimulationScope, ? extends Fleet> fleet
  ) {
    return new SocialNetworkFactory(fleet);
  }

  /**
   * @param network               the network whose ties are the friendships
   * @param targetNumberOfFriends how many friends a vessel recruits, a value
   *                              of the vessel read once at each of its
   *                              fishing trip starts
   * @param friendshipCondition   which other vessels count as friends of a
   *                              vessel: a mapping of the other vessel, read
   *                              with {@code other(...)} (see
   *                              {@link uk.ac.ox.poseidon.fishing.vessels.values.Factories#other})
   * @return a
   *         {@link uk.ac.ox.poseidon.core.events.SimulationEventListenerFactory}
   *         for a {@link FriendshipDynamics} registered with the simulation's
   *         event manager, so that it hears every vessel's fishing trip starts
   * @see FriendshipDynamics
   */
  public static FriendshipDynamicsFactory friendshipDynamics(
    final Factory<? super SimulationScope, ? extends SocialNetwork> network,
    final ValueFactory<
      ? super SimulationScope,
      ? super Vessel,
      ? extends Number
    > targetNumberOfFriends,
    final MappingFactory<
      ? super SimulationScope,
      ? super Vessel,
      ? super Vessel,
      Boolean
    > friendshipCondition
  ) {
    return new FriendshipDynamicsFactory(
      network,
      targetNumberOfFriends,
      friendshipCondition,
      rng()
    );
  }

  /**
   * @param network               the network whose ties are the friendships
   * @param targetNumberOfFriends how many friends every vessel recruits
   * @param friendshipCondition   which other vessels count as friends of a
   *                              vessel: a mapping of the other vessel, read
   *                              with {@code other(...)}
   * @return the same as {@link #friendshipDynamics(Factory, ValueFactory,
   *         MappingFactory)}, with the target a constant
   */
  public static FriendshipDynamicsFactory friendshipDynamics(
    final Factory<? super SimulationScope, ? extends SocialNetwork> network,
    final int targetNumberOfFriends,
    final MappingFactory<
      ? super SimulationScope,
      ? super Vessel,
      ? super Vessel,
      Boolean
    > friendshipCondition
  ) {
    return friendshipDynamics(
      network,
      constant(targetNumberOfFriends),
      friendshipCondition
    );
  }

  /**
   * @param network           the network whose ties the dynamics add and remove
   * @param groupingValues    give the values that group vessels: vessels are
   *                          eligible for each other when both are present and
   *                          each of these gives them equal values
   * @param maximumCliqueSize the largest clique a vessel joins, itself
   *                          included; 1 means no vessel ever has partners
   * @return a
   *         {@link uk.ac.ox.poseidon.core.events.SimulationEventListenerFactory}
   *         for a {@link CliqueDynamics} registered with the simulation's event
   *         manager, so that it hears every vessel's trip starts
   * @see CliqueDynamics
   */
  public static CliqueDynamicsFactory cliqueDynamics(
    final Factory<? super SimulationScope, ? extends SocialNetwork> network,
    final List<
      ? extends ValueFactory<? super SimulationScope, ? super Vessel, ?>
    > groupingValues,
    final int maximumCliqueSize
  ) {
    return new CliqueDynamicsFactory(
      network,
      ImmutableList.copyOf(groupingValues),
      maximumCliqueSize,
      rng()
    );
  }

  /**
   * @param network the network whose ties decide who hears each fishing action
   * @return a
   *         {@link uk.ac.ox.poseidon.core.events.SimulationEventListenerFactory}
   *         for an {@link EventSharer} of {@link FishingActionEvent}s
   *         registered with the simulation's event manager
   * @see EventSharer
   */
  public static EventSharerFactory<FishingActionEvent> fishingActionSharer(
    final Factory<? super SimulationScope, ? extends SocialNetwork> network
  ) {
    return new EventSharerFactory<>(
      FishingActionEvent.class.getName(),
      network
    );
  }

  /**
   * @param network the network whose ties decide who hears each fishing trip
   *                end
   * @return a
   *         {@link uk.ac.ox.poseidon.core.events.SimulationEventListenerFactory}
   *         for an {@link EventSharer} of {@link FishingTripEndEvent}s
   *         registered with the simulation's event manager
   * @see EventSharer
   */
  public static EventSharerFactory<FishingTripEndEvent> fishingTripEndSharer(
    final Factory<? super SimulationScope, ? extends SocialNetwork> network
  ) {
    return new EventSharerFactory<>(
      FishingTripEndEvent.class.getName(),
      network
    );
  }

  /**
   * @param listener            the factory giving the vessel's shared listener
   * @param fishingActionSharer the sharer to register the listener with
   * @return a {@link uk.ac.ox.poseidon.fishing.vessels.VesselComponentScopeFactory}
   *         that registers the vessel's listener with the sharer and gives it;
   *         make it one of the vessel's components
   * @see SharedEventListenerFactory
   */
  public static <L extends Listener<? super FishingActionEvent>> SharedEventListenerFactory<
    FishingActionEvent,
    L
  > sharedFishingActionListener(
    final Factory<? super VesselComponentScope, ? extends L> listener,
    final Factory<
      ? super SimulationScope,
      ? extends EventSharer<FishingActionEvent>
    > fishingActionSharer
  ) {
    return new SharedEventListenerFactory<>(listener, fishingActionSharer);
  }

  /**
   * @param listener             the factory giving the vessel's shared listener
   * @param fishingTripEndSharer the sharer to register the listener with
   * @return a {@link uk.ac.ox.poseidon.fishing.vessels.VesselComponentScopeFactory}
   *         that registers the vessel's listener with the sharer and gives it;
   *         make it one of the vessel's components
   * @see SharedEventListenerFactory
   */
  public static <L extends Listener<? super FishingTripEndEvent>> SharedEventListenerFactory<
    FishingTripEndEvent,
    L
  > sharedFishingTripEndListener(
    final Factory<? super VesselComponentScope, ? extends L> listener,
    final Factory<
      ? super SimulationScope,
      ? extends EventSharer<FishingTripEndEvent>
    > fishingTripEndSharer
  ) {
    return new SharedEventListenerFactory<>(listener, fishingTripEndSharer);
  }
}
