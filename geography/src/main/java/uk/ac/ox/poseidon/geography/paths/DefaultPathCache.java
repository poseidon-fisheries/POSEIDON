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

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;

/**
 * The default {@link PathCache}: an in-memory, unbounded Caffeine {@link Cache}
 * keyed by (start, end) pairs. Built via
 * {@link Factories#pathCache(uk.ac.ox.poseidon.core.Factory, uk.ac.ox.poseidon.core.Factory, uk.ac.ox.poseidon.core.Factory)}
 * in this package.
 */
public class DefaultPathCache<P> implements PathCache<P> {

  // Keyed by List.of(start, end): Map.entry's hash, the XOR of the two points'
  // hashes, collides badly on grid cells (A to B and B to A always collide).
  private final Cache<List<P>, Optional<ImmutableList<P>>> memory =
    Caffeine.newBuilder().build();

  /**
   * @param start The point at which the path should start
   * @param end   The point at which the path should end
   * @return Either:
   *         <ul>
   *         <li>An immutable list of sea tiles wrapped in an Optional if a path
   *         from start to end was stored;</li>
   *         <li>An empty Optional if start to end was stored as
   *         impossible;</li>
   *         <li>null if nothing was stored for start to end.</li>
   *         </ul>
   */
  @Override
  public Optional<ImmutableList<P>> getPath(final P start, final P end) {
    return memory.getIfPresent(List.of(start, end));
  }

  @Override
  public void putPath(
    final P start,
    final P end,
    final Optional<ImmutableList<P>> path
  ) {
    memory.put(List.of(start, end), path);
  }
}
