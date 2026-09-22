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

import java.util.Optional;

/**
 * Finds a path between two points of type {@code P}. Implementations in this package compose:
 * {@link BresenhamPathFinder} (a fast straight-line heuristic) and {@link AStarPathFinder} (a
 * slower, complete search) are combined by {@link FallbackPathfinder}, and the result is wrapped
 * in a {@link CachingPathFinder}. Built via {@link uk.ac.ox.poseidon.geography.paths.Factories}.
 */
public interface PathFinder<P> {
    /**
     * @param start The starting point of the path
     * @param end   The ending point of the path
     * @return The path from {@code start} to {@code end}, as an {@link ImmutableList} wrapped in an
     * {@link Optional}. An empty {@link Optional} denotes an impossible path, while returning
     * {@code null} denotes that no path was found by this particular pathfinder, though one might
     * still exist. Note that not every implementation in this package honors the {@code null} case
     * strictly: {@link BresenhamPathFinder} returns an empty {@link Optional} whenever its
     * straight-line search is blocked, even though a longer path might still exist — this is
     * harmless in the {@link FallbackPathfinder} composition, since {@link Optional#or} treats an
     * empty result as "try the next pathfinder" either way, but means
     * {@link BresenhamPathFinder} shouldn't be relied on standalone to mean "provably impossible."
     */
    Optional<ImmutableList<P>> getPath(
        P start,
        P end
    );

}
