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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;

import java.util.Map.Entry;
import java.util.Optional;

import static java.util.Map.entry;

public class DefaultPathCache<P> implements PathCache<P> {

    private final Cache<Entry<P, P>, Optional<ImmutableList<P>>> memory =
        CacheBuilder.newBuilder().build();

    /**
     * Returns a path from start to end if one is known. Otherwise, checks for a path from end to
     * start that we can reverse, storing it and returning it if there is one. Returned paths are
     * wrapped in Optional. An empty optional means the path is impossible.
     *
     * @param start The N at which the path should start
     * @param end   The N at which the path should end
     * @return Either:
     * <ul>
     * <li>An immutable list of sea tiles wrapped in an Optional if there is a known path between
     * start and end;</li>
     * <li>An empty Optional if we know there is no path between start and end;</li>
     * <li>null if we don't know anything about this path.</li>
     * </ul>
     */
    @Override
    @SuppressWarnings("OptionalAssignedToNull")
    public Optional<ImmutableList<P>> getPath(
        final P start,
        final P end
    ) {
        final Optional<ImmutableList<P>> knownPath =
            memory.getIfPresent(entry(start, end));
        if (knownPath != null)
            return knownPath;
        else {
            // maybe we have it in reverse?
            final Optional<ImmutableList<P>> knownInversePath =
                memory.getIfPresent(entry(end, start));
            if (knownInversePath != null) {
                // We have the path we want (possible or not) in reverse so we can
                // reverse it, memorise it and return it
                final Optional<ImmutableList<P>> newPath =
                    knownInversePath.map(ImmutableList::reverse);
                putPath(start, end, newPath);
                return newPath;
            }
        }
        // We do not know anything about this path
        return null;
    }

    @Override
    public void putPath(
        final P start,
        final P end,
        final Optional<ImmutableList<P>> path
    ) {
        path.ifPresentOrElse(
            this::putPath,
            () -> putImpossiblePath(start, end)
        );
    }

    /**
     * This method stores the path in the cache, but it also stores all the intermediate sublists
     * along the path. For example, if {@code A -> B -> C -> D} is the shortest path from A to D,
     * {@code B -> C -> D} is the shortest path from B to D, and {@code C -> D} is also (trivially)
     * the shortest path from C to D. Since intermediate paths are stored as sublists, they won't
     * take up space besides their keys in the cache.
     *
     * @param path An immutable list of points forming the path
     */
    @Override
    public void putPath(
        final ImmutableList<P> path
    ) {
        final int n = path.size();
        final P end = path.getLast();
        for (int i = 0; i < n - 1; i++) {
            memory.put(
                entry(path.get(i), end),
                Optional.of(path.subList(i, n))
            );
        }
    }

    @Override
    public void putImpossiblePath(
        final P start,
        final P end
    ) {
        memory.put(entry(start, end), Optional.empty());
    }

}
