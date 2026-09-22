/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024-2025, University of Oxford.
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
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.Optional;

/**
 * A {@link PathFinder} that delegates to another {@link PathFinder}, consulting and updating a
 * {@link PathCache} so repeated queries for the same start/end are served from memory. Built via
 * {@link uk.ac.ox.poseidon.geography.paths.Factories}.
 */
public class CachingPathFinder<P> implements PathFinder<P> {
    private final PathFinder<P> pathFinder;
    private final PathCache<P> cache;

    /**
     * @param pathFinder the delegate consulted on a cache miss
     * @param cache      the cache consulted first, and updated with any new result
     */
    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public CachingPathFinder(
        final PathFinder<P> pathFinder,
        final PathCache<P> cache
    ) {
        this.pathFinder = pathFinder;
        this.cache = cache;
    }

    @Override
    public Optional<ImmutableList<P>> getPath(
        final P start,
        final P end
    ) {
        return Optional
            .ofNullable(cache.getPath(start, end))
            .orElseGet(() -> {
                final Optional<ImmutableList<P>> newPath = pathFinder.getPath(start, end);
                cache.putPath(start, end, newPath);
                return newPath;
            });
    }
}
