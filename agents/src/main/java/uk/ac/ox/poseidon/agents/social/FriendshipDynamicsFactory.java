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

import static com.google.common.base.Preconditions.checkNotNull;

import ec.util.MersenneTwisterFast;
import java.util.Set;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.MappingFactory;
import uk.ac.ox.poseidon.core.SimulationScope;
import uk.ac.ox.poseidon.core.SimulationScopeFactory;
import uk.ac.ox.poseidon.core.ValueFactory;

/**
 * A {@link SimulationScopeFactory} for {@link FriendshipDynamics}, built via
 * {@link Factories#friendshipDynamics}: a value of a vessel, its friends. The
 * dynamics use the simulation's random number generator.
 */
@Getter
@ToString
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
public class FriendshipDynamicsFactory
  extends SimulationScopeFactory<Function<Vessel, Set<Vessel>>>
  implements ValueFactory<SimulationScope, Vessel, Set<Vessel>>
{

  private final Factory<
    ? super SimulationScope,
    ? extends SocialNetwork
  > network;
  private final int targetNumberOfFriends;
  private final MappingFactory<
    ? super SimulationScope,
    ? super Vessel,
    ? super Vessel,
    Boolean
  > friendshipCondition;
  private final Factory<
    ? super SimulationScope,
    ? extends MersenneTwisterFast
  > randomNumberGenerator;

  @Override
  protected Function<Vessel, Set<Vessel>> newInstance(
    final SimulationScope scope
  ) {
    checkNotNull(network, "network must not be null");
    checkNotNull(friendshipCondition, "friendshipCondition must not be null");
    return new FriendshipDynamics(
      network.get(scope),
      targetNumberOfFriends,
      friendshipCondition.get(scope),
      randomNumberGenerator.get(scope)
    );
  }
}
