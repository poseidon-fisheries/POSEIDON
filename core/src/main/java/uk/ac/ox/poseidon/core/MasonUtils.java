/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024 CoHESyS Lab cohesys.lab@gmail.com
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
 *
 */

package uk.ac.ox.poseidon.core;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import ec.util.MersenneTwisterFast;
import sim.field.continuous.Continuous2D;
import sim.field.geo.GeomField;
import sim.field.grid.Grid2D;
import sim.util.Bag;
import sim.util.Double2D;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static uk.ac.ox.poseidon.core.utils.Preconditions.checkNonNegative;

public class MasonUtils {

    public static <T> ImmutableSet<T> bagToSet(final Bag bag) {
        return MasonUtils.<T>bagToStream(bag).collect(toImmutableSet());
    }

    @SuppressWarnings("unchecked")
    public static <T> Stream<T> bagToStream(final Bag bag) {
        return Optional
            .ofNullable(bag)
            .stream()
            .flatMap(b -> IntStream
                .range(0, b.size())
                .mapToObj(i -> (T) b.get(i))
            );
    }

    public static Object oneOf(
        final Bag candidates,
        final MersenneTwisterFast random
    ) {
        return candidates.get(oneOfIndices(candidates, random));
    }

    private static int oneOfIndices(
        @SuppressWarnings("rawtypes") final Collection candidates,
        final MersenneTwisterFast random
    ) {
        final int n = candidates.size();
        return n == 1 ? 0 : random.nextInt(n);
    }

    public static <T> T oneOf(
        final T[] candidates,
        final MersenneTwisterFast random
    ) {
        return candidates[oneOfIndices(candidates, random)];
    }

    private static <T> int oneOfIndices(
        final T[] candidates,
        final MersenneTwisterFast random
    ) {
        validateCandidates(candidates);
        final int n = candidates.length;
        return n == 1 ? 0 : random.nextInt(n);
    }

    private static <T> void validateCandidates(final T[] candidates) {
        checkNotNull(candidates, "collection of candidates must not be null");
        checkArgument(candidates.length > 0, "collection of must not be empty");
    }

    public static <T> T oneOf(
        final List<T> candidates,
        final MersenneTwisterFast random
    ) {
        return candidates.get(oneOfIndices(candidates, random));
    }

    public static <T> Optional<T> upToOneOf(
        final List<T> candidates,
        final MersenneTwisterFast random
    ) {
        return Optional
            .ofNullable(candidates)
            .filter(xs -> !xs.isEmpty())
            .map(xs -> xs.get(oneOfIndices(candidates, random)));
    }

    public static <T> ImmutableList<T> upToNOf(
        final int n,
        final List<T> candidates,
        final MersenneTwisterFast rng
    ) {
        checkNonNegative(n, "n");
        if (n == 0 || candidates.isEmpty()) return ImmutableList.of();
        final int size = candidates.size();
        if (n < candidates.size()) return ImmutableList.copyOf(candidates);
        final ImmutableList.Builder<T> builder = ImmutableList.builder();
        int i = 0;
        int j = 0;
        while (j < n && i < size) {
            if (rng.nextInt(size - i) < n - j) {
                builder.add(candidates.get(i));
                j += 1;
            }
            i += 1;
        }
        return builder.build();
    }

    public static boolean inBounds(
        final Double2D location,
        final Continuous2D continuous2D
    ) {
        return inBounds(location, continuous2D.getWidth(), continuous2D.getHeight());
    }

    private static boolean inBounds(
        final Double2D location,
        final double fieldWidth,
        final double fieldHeight
    ) {
        return location.x >= 0 &&
            location.x < fieldWidth &&
            location.y >= 0 &&
            location.y < fieldHeight;
    }

    public static boolean inBounds(
        final Double2D location,
        final Grid2D grid2D
    ) {
        return inBounds(location, grid2D.getWidth(), grid2D.getHeight());
    }

    public static boolean inBounds(
        final Double2D location,
        final GeomField geomField
    ) {
        return inBounds(location, geomField.getWidth(), geomField.getHeight());
    }

    public static <T> Stream<T> shuffledStream(
        final List<T> candidates,
        final MersenneTwisterFast rng
    ) {
        final BitSet visited = new BitSet(candidates.size()); // Track visited indices
        final Spliterator<T> spliterator = new Spliterators.AbstractSpliterator<>(
            candidates.size(),
            Spliterator.ORDERED
        ) {
            private int remaining = candidates.size();

            @Override
            public boolean tryAdvance(final Consumer<? super T> action) {
                if (remaining == 0) {
                    return false; // All elements have been visited
                }
                int index;
                do {
                    index = rng.nextInt(candidates.size());
                } while (visited.get(index)); // Ensure no duplicates
                visited.set(index); // Mark index as visited
                remaining--;
                action.accept(candidates.get(index)); // Provide the element
                return true;
            }
        };
        return StreamSupport.stream(spliterator, false);
    }
}
