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

import java.util.Collection;
import java.util.Optional;

public interface PathCache<P> extends PathFinder<P> {

    /**
     * Turns a path provided as a {@link Collection} into and {@link ImmutableList} before putting
     * it into memory. For this to make sense, the collection must be ordered (i.e., be a
     * {@link java.util.List}, a {@link java.util.Queue}, or a {@link java.util.SortedSet}).
     *
     * @param path An ordered collection of points forming the path
     */
    default void putPath(
        final Collection<P> path
    ) {
        putPath(ImmutableList.copyOf(path));
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    void putPath(
        P start,
        P end,
        Optional<ImmutableList<P>> path
    );

    /**
     * Puts a new path in memory.
     *
     * @param path An immutable list of points forming the path
     */
    void putPath(
        ImmutableList<P> path
    );

    /**
     * Records the fact that there is no possible path from {@code start} to {@code end}.
     *
     * @param start The starting node of the path
     * @param end   The ending node of the path
     */
    void putImpossiblePath(
        P start,
        P end
    );
}
