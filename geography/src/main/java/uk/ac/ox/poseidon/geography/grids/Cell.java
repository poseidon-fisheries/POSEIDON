/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2026, University of Oxford.
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

package uk.ac.ox.poseidon.geography.grids;

import lombok.Value;
import uk.ac.ox.poseidon.geography.Destination;
import uk.ac.ox.poseidon.geography.ports.PortGrid;

/**
 * A cell of the model grid. Two cells are equal when they have the same
 * column and row.
 */
@Value
public final class Cell implements Destination {

  /** The cell's column, from the west. */
  int x;

  /** The cell's row, from the north. */
  int y;

  /** @return this cell */
  @Override
  public Cell getCell(final PortGrid portGrid) {
    return this;
  }

  /** @return whether {@code other} is a cell with the same column and row */
  @Override
  public boolean equals(final Object other) {
    return other instanceof final Cell cell && x == cell.x && y == cell.y;
  }

  /**
   * The same hash as MASON's {@code Int2D}: the row goes through a bit mix
   * before it is combined with the column. A generated hash would give many
   * cells of a grid the same hash code, and hash maps keyed by cells would slow
   * down.
   */
  @Override
  public int hashCode() {
    int hash = y;
    hash += ~(hash << 15);
    hash ^= hash >>> 10;
    hash += hash << 3;
    hash ^= hash >>> 6;
    hash += ~(hash << 11);
    hash ^= hash >>> 16;
    return x ^ hash;
  }
}
