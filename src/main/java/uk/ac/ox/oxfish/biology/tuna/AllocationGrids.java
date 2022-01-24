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

package uk.ac.ox.oxfish.biology.tuna;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
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
import java.util.function.IntUnaryOperator;
import sim.field.grid.DoubleGrid2D;

/**
 * An allocation grid is basically just a DoubleGrid2D, normalized to sum up to one. This class does
 * a bit more, however: it puts the grids in a schedule (just a map from ints) and, for each
 * scheduled entry, associate grids with a key. That key can be, e.g., the species name (as used in
 * the {@link BiomassReallocator} or a combination of the species name and the size group (as in
 * {@link AbundanceReallocator}). We use species names instead of {@link
 * uk.ac.ox.oxfish.biology.Species} objects so that a single set of grids can be shared between
 * parallel simulation runs.
 *
 * @param <K> The type of key to use to identify which grid to use.
 */
public class AllocationGrids<K> {

    // map from step to key to grid
    private final ImmutableSortedMap<Integer, Map<K, DoubleGrid2D>> grids;
    private final IntUnaryOperator stepMapper;

    private AllocationGrids(
        final ImmutableSortedMap<Integer, Map<K, DoubleGrid2D>> grids,
        final int period
    ) {
        // Make sure all grids sum to 1.
        checkArgument(
            grids.values()
                .stream()
                .flatMap(map -> map.values().stream())
                .allMatch(grid -> fuzzyEquals(Arrays.stream(grid.toArray()).sum(), 1.0, EPSILON))
        );
        this.grids = grids;
        this.stepMapper = new PeriodicStepMapper(period);
    }

    /**
     * Creates a new {@link AllocationGrids} object.
     *
     * @param grids  The grids to use. The method will copy them in a sorted map.
     * @param period The period to use (likely 365) to map simulation step to grid schedule
     *               entries.
     * @param <K>    The type of key to use to identify grids (likely species names or species name
     *               plus size category).
     * @return A new {@link AllocationGrids} object.
     */
    public static <K> AllocationGrids<K> from(
        final Map<Integer, ? extends Map<K, DoubleGrid2D>> grids,
        final int period
    ) {
        return new AllocationGrids<>(
            grids.entrySet().stream()
                .collect(toImmutableSortedMap(
                    Comparator.naturalOrder(),
                    Entry::getKey,
                    entry -> ImmutableMap.copyOf(entry.getValue())
                )),
            period
        );
    }

    IntUnaryOperator getStepMapper() {
        return stepMapper;
    }

    public ImmutableSortedMap<Integer, Map<K, DoubleGrid2D>> getGrids() {
        return grids;
    }

    Map<K, DoubleGrid2D> atOrBeforeStep(final Integer step) {
        final Entry<? super Integer, Map<K, DoubleGrid2D>> floorEntry =
            grids.floorEntry(stepMapper.applyAsInt(step));
        checkNotNull(floorEntry, "No grids at or before step " + step);
        return floorEntry.getValue();
    }

    public int size() {
        return grids.size();
    }

    public Collection<Map<K, DoubleGrid2D>> values() {
        return grids.values();
    }

}
