/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024-2026, University of Oxford.
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

import static com.google.common.base.Preconditions.checkNotNull;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.collect.ImmutableList;
import java.util.Optional;
import uk.ac.ox.poseidon.geography.grids.Cell;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;

/**
 * The default {@link PathCache}: an in-memory, unbounded Caffeine {@link Cache}
 * keyed by (start, end) pairs of cells of one {@link ModelGrid}. Built via
 * {@link Factories#pathCache(uk.ac.ox.poseidon.core.Factory, uk.ac.ox.poseidon.core.Factory, uk.ac.ox.poseidon.core.Factory)}
 * in this package.
 */
public class DefaultPathCache implements PathCache<Cell> {

  private final ModelGrid modelGrid;
  // Keyed by the two cells' ModelGrid.cellPairKey: object keys cost hashing
  // and comparing two cells on every lookup.
  private final Cache<Long, Optional<ImmutableList<Cell>>> memory =
    Caffeine.newBuilder().build();

  /** @param modelGrid the grid whose cells the cached paths go through */
  DefaultPathCache(final ModelGrid modelGrid) {
    this.modelGrid = checkNotNull(modelGrid);
  }

  /**
   * @param start The cell at which the path should start
   * @param end   The cell at which the path should end
   * @return Either:
   *         <ul>
   *         <li>An immutable list of sea tiles wrapped in an Optional if a path
   *         from start to end was stored;</li>
   *         <li>An empty Optional if start to end was stored as
   *         impossible;</li>
   *         <li>null if nothing was stored for start to end.</li>
   *         </ul>
   * @throws IllegalArgumentException if either cell is outside the grid
   */
  @Override
  public Optional<ImmutableList<Cell>> getPath(
    final Cell start,
    final Cell end
  ) {
    return memory.getIfPresent(modelGrid.cellPairKey(start, end));
  }

  /** @throws IllegalArgumentException if either cell is outside the grid */
  @Override
  public void putPath(
    final Cell start,
    final Cell end,
    final Optional<ImmutableList<Cell>> path
  ) {
    memory.put(modelGrid.cellPairKey(start, end), path);
  }
}
