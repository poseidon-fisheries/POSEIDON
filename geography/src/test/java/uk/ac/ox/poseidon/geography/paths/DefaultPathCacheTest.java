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


package uk.ac.ox.poseidon.geography.paths;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link DefaultPathCache}'s contract: {@code null} for a path it knows nothing about, an empty
 * {@code Optional} for one it knows is impossible, and the path otherwise. Points are letters, so
 * {@code path("A", "B", "C")} goes from A to C through B.
 */
class DefaultPathCacheTest {

    private final DefaultPathCache<String> cache = new DefaultPathCache<>();

    private static ImmutableList<String> path(final String... points) {
        return ImmutableList.copyOf(points);
    }

    @Test
    void anUnknownPathIsNull() {
        assertThat(cache.getPath("A", "B")).isNull();
    }

    @Test
    void aStoredPathIsReturned() {
        cache.putPath("A", "C", Optional.of(path("A", "B", "C")));
        assertThat(cache.getPath("A", "C")).contains(path("A", "B", "C"));
    }

    @Test
    void anImpossiblePathIsEmptyNotNull() {
        cache.putPath("A", "B", Optional.empty());
        assertThat(cache.getPath("A", "B")).isNotNull().isEmpty();
    }

    @Test
    void onlyTheStoredStartAndEndAreKnown() {
        cache.putPath("A", "D", Optional.of(path("A", "B", "C", "D")));
        cache.putPath("E", "F", Optional.empty());
        // Neither reversed nor a tail of a stored path
        assertThat(cache.getPath("D", "A")).isNull();
        assertThat(cache.getPath("B", "D")).isNull();
        assertThat(cache.getPath("F", "E")).isNull();
    }

    @Test
    void aLaterPathReplacesAnEarlierOne() {
        cache.putPath("A", "C", Optional.empty());
        cache.putPath("A", "C", Optional.of(path("A", "B", "C")));
        assertThat(cache.getPath("A", "C")).contains(path("A", "B", "C"));
        cache.putPath("A", "C", Optional.of(path("A", "D", "C")));
        assertThat(cache.getPath("A", "C")).contains(path("A", "D", "C"));
    }
}
