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

import ec.util.MersenneTwisterFast;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.agents.vessels.VesselsGetter;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static java.util.function.Predicate.not;
import static uk.ac.ox.poseidon.core.MasonUtils.shuffledStream;

/**
 * A per-vessel supplier that maintains a bounded, randomly sampled set of "friends" from a pool of
 * candidate vessels.
 *
 * <p>Each call to {@link #get()} first prunes friends that no longer satisfy the friendship
 * condition. If the target count has not been reached, new friends are sampled uniformly (without
 * replacement) from the pool, filtered by the condition and excluding current friends.
 *
 * <p>The sampling uses {@link uk.ac.ox.poseidon.core.MasonUtils#shuffledStream shuffledStream}
 * combined with {@code limit}, which evaluates the friendship condition lazily — only as many
 * candidates as needed to reach the target are examined. This minimizes predicate cost at high
 * acceptance rates, but allocates O(pool size) per call for the shuffle index array and the pool
 * copy. If the pool grows large and the condition is cheap, consider replacing with reservoir
 * sampling for O(target) allocation at the cost of O(pool) predicate evaluations.
 */
@RequiredArgsConstructor
public class DynamicFriendsSupplier implements Supplier<Set<Vessel>> {

    private final int targetNumberOfFriends;
    @NonNull private final VesselsGetter potentialFriends;
    @NonNull private final Predicate<? super Vessel> friendshipCondition;
    @NonNull private final Vessel vessel;
    @NonNull private final MersenneTwisterFast rng;

    private final Set<Vessel> currentFriends = new HashSet<>();

    @Override
    public Set<Vessel> get() {

        currentFriends.removeIf(not(friendshipCondition));

        if (currentFriends.size() < targetNumberOfFriends) {
            shuffledStream(potentialFriends.getVessels(), rng)
                .filter(vessel -> vessel != this.vessel)
                .filter(not(currentFriends::contains))
                .filter(friendshipCondition)
                .limit(targetNumberOfFriends - currentFriends.size())
                .forEach(currentFriends::add);
        }

        return Collections.unmodifiableSet(currentFriends);
    }
}
