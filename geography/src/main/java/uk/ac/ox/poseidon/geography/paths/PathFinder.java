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

import java.util.Optional;

public interface PathFinder<P> {
    /**
     * @param start The starting point of the path
     * @param end   The ending point of the path
     * @return The path from {@code start} to {@code end}, as an {@link ImmutableList} wrapped in an
     * {@link Optional}. An empty {@link Optional} denotes an impossible path, while returning
     * {@code null} denotes that no path was found by this particular pathfinder, though one might
     * still exist.
     */
    Optional<ImmutableList<P>> getPath(
        P start,
        P end
    );

}
