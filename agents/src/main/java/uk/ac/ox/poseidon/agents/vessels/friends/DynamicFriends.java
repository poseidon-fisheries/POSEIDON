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

import static uk.ac.ox.poseidon.core.MasonUtils.reservoirSample;

import ec.util.MersenneTwisterFast;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.agents.vessels.VesselsGetter;

/**
 * The friends of a vessel: a bounded, randomly sampled set of "friends" from a
 * pool of candidate vessels, kept for one vessel.
 *
 * <p>
 * Each call to {@link #apply} first prunes friends that no longer satisfy the
 * friendship condition, which relates the vessel given to each other vessel. If
 * the target count has not been reached, new friends are sampled uniformly
 * (without replacement) from the pool, filtered by the condition and excluding
 * the vessel and its current friends.
 *
 * <p>
 * The sampling uses reservoir sampling
 * ({@link uk.ac.ox.poseidon.core.MasonUtils#reservoirSample reservoirSample})
 * for O(target) allocation at the cost of O(pool) predicate evaluations.
 */
@RequiredArgsConstructor
public class DynamicFriends implements Function<Vessel, Set<Vessel>> {

  private final int targetNumberOfFriends;

  @NonNull
  private final VesselsGetter potentialFriends;

  @NonNull
  private final BiFunction<
    ? super Vessel,
    ? super Vessel,
    Boolean
  > friendshipCondition;

  @NonNull
  private final MersenneTwisterFast rng;

  // Insertion order, not hash order: vessels hash by identity
  private final Set<Vessel> currentFriends = new LinkedHashSet<>();

  @Override
  public Set<Vessel> apply(final Vessel vessel) {
    currentFriends.removeIf(v -> !friendshipCondition.apply(vessel, v));

    if (currentFriends.size() < targetNumberOfFriends) {
      reservoirSample(
        potentialFriends.getVessels(),
        targetNumberOfFriends - currentFriends.size(),
        v ->
          v != vessel &&
          !currentFriends.contains(v) &&
          friendshipCondition.apply(vessel, v),
        rng
      ).forEach(currentFriends::add);
    }

    return Collections.unmodifiableSet(currentFriends);
  }
}
