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

package uk.ac.ox.poseidon.agents.vessels.friends;

import static uk.ac.ox.poseidon.core.providers.random.Factories.randomNumberGenerator;

import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.agents.vessels.VesselScope;
import uk.ac.ox.poseidon.agents.vessels.VesselsGetter;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.MappingFactory;

/**
 * Factories for a vessel's dynamically-maintained set of "friend" vessels.
 */
public class Factories {

  private Factories() {}

  /**
   * @param targetNumberOfFriends how many friends to keep
   * @param potentialFriends      the vessels friends are drawn from
   * @param friendshipCondition   which other vessels can be friends of a
   *                              vessel: a mapping of the other vessel, read
   *                              with {@code other(...)} (see
   *                              {@link uk.ac.ox.poseidon.agents.vessels.values.Factories#other})
   * @return a {@link uk.ac.ox.poseidon.agents.vessels.VesselScopeFactory} for
   *         {@link DynamicFriends}
   * @see DynamicFriendsFactory
   */
  public static DynamicFriendsFactory dynamicFriends(
    final int targetNumberOfFriends,
    final Factory<
      ? super VesselScope,
      ? extends VesselsGetter
    > potentialFriends,
    final MappingFactory<
      ? super VesselScope,
      ? super Vessel,
      ? super Vessel,
      Boolean
    > friendshipCondition
  ) {
    return new DynamicFriendsFactory(
      targetNumberOfFriends,
      potentialFriends,
      friendshipCondition,
      randomNumberGenerator()
    );
  }
}
