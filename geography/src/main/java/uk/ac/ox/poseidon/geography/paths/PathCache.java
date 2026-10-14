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
 * A {@link PathFinder} that also remembers paths (and the absence thereof) it's
 * told about, so repeated queries for the same start/end don't require
 * recomputation. It answers only for the exact start and end it was told about:
 * never with a reversed path or the tail of a longer one, which could differ
 * from what a path finder returns for that start and end. A cached answer thus
 * never depends on which queries came before, even when simulations share the
 * cache. Built via {@link uk.ac.ox.poseidon.geography.paths.Factories};
 * implemented by {@link DefaultPathCache}.
 */
public interface PathCache<P> extends PathFinder<P> {
  /**
   * @param start the starting point of the path
   * @param end   the ending point of the path
   * @param path  the path to remember: present for a known path, empty for a
   *              known-impossible one
   */
  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  void putPath(P start, P end, Optional<ImmutableList<P>> path);
}
