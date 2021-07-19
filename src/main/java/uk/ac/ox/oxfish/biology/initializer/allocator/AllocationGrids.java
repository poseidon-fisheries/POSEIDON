/*
 * POSEIDON, an agent-based model of fisheries
 * Copyright (C) 2021 CoHESyS Lab cohesys.lab@gmail.com
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.biology.initializer.allocator;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableSortedMap.toImmutableSortedMap;
import static com.google.common.math.DoubleMath.fuzzyEquals;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.EPSILON;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import sim.field.grid.DoubleGrid2D;

class AllocationGrids<K> {

    // map from step to species name to grid
    private final ImmutableSortedMap<? super Integer, Map<K, DoubleGrid2D>> grids;

    AllocationGrids(
        final ImmutableSortedMap<? super Integer, Map<K, DoubleGrid2D>> grids
    ) {
        // Make sure all grids sum to 1.
        checkArgument(
            grids.values()
                .stream()
                .flatMap(map -> map.values().stream())
                .allMatch(grid -> fuzzyEquals(Arrays.stream(grid.toArray()).sum(), 1.0, EPSILON))
        );
        this.grids = grids;
    }

    public static <K> AllocationGrids<K> from(
        final Map<Integer, ? extends Map<K, DoubleGrid2D>> grids
    ) {
        return new AllocationGrids<>(grids.entrySet().stream()
            .collect(toImmutableSortedMap(
                Comparator.naturalOrder(),
                Entry::getKey,
                entry -> ImmutableMap.copyOf(entry.getValue())
            ))
        );
    }

    Optional<Map<K, DoubleGrid2D>> atStep(final Integer step) {
        return Optional.ofNullable(grids.get(step));
    }

    Optional<Map<K, DoubleGrid2D>> atOrBeforeStep(final Integer step) {
        return Optional.ofNullable(grids.floorEntry(step).getValue());
    }

    public int size() {
        return grids.size();
    }

    public Collection<Map<K, DoubleGrid2D>> values() {
        return grids.values();
    }

}
