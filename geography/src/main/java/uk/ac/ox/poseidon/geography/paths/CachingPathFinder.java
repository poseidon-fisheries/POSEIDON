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

package uk.ac.ox.poseidon.geography.paths;

import com.google.common.collect.ImmutableList;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.Optional;

public class CachingPathFinder<P> implements PathFinder<P> {
    private final PathFinder<P> pathFinder;
    private final PathCache<P> cache;

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
