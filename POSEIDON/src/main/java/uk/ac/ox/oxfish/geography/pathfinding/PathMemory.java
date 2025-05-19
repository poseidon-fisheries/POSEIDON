/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

package uk.ac.ox.oxfish.geography.pathfinding;

import com.google.common.collect.ImmutableList;
import uk.ac.ox.oxfish.geography.SeaTile;

import java.util.Collection;
import java.util.Optional;

public interface PathMemory {

    /**
     * @param start The starting {@link SeaTile} of the path
     * @param end   The ending {@link SeaTile} of the path
     * @return The path from {@code start} to {@code end}, as an {@link ImmutableList} wrapped in an {@link Optional},
     * if we have it in memory. Returns {@code null} otherwise. An empty {@link Optional} denotes an impossible path.
     */
    Optional<ImmutableList<SeaTile>> getPath(SeaTile start, SeaTile end);

    /**
     * Turns a path provided as a {@link Collection} into and {@link ImmutableList} before putting it into memory.
     * For this to make sense, the collection must be ordered (i.e., be a {@link java.util.List},
     * a {@link java.util.Queue}, or a {@link java.util.SortedSet}).
     *
     * @param start The starting {@link SeaTile} of the path
     * @param end   The ending {@link SeaTile} of the path
     * @param path  An ordered collection of {@link SeaTile} forming the path
     */
    default void putPath(SeaTile start, SeaTile end, Collection<SeaTile> path) {
        putPath(start, end, ImmutableList.copyOf(path));
    }

    /**
     * Puts a new path in memory.
     *
     * @param start The starting {@link SeaTile} of the path
     * @param end   The ending {@link SeaTile} of the path
     * @param path  An {@link ImmutableList} of {@link SeaTile}.
     */
    void putPath(SeaTile start, SeaTile end, ImmutableList<SeaTile> path);

    /**
     * Records the fact that there is no possible path from {@code start} to {@code end}.
     *
     * @param start The starting {@link SeaTile} of the path
     * @param end   The ending {@link SeaTile} of the path
     */
    void putImpossiblePath(SeaTile start, SeaTile end);
}
