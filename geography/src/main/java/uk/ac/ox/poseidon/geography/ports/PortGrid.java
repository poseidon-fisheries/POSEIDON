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

package uk.ac.ox.poseidon.geography.ports;

import static com.google.common.base.Preconditions.checkState;

import java.util.Optional;
import java.util.stream.Stream;
import uk.ac.ox.poseidon.geography.Coordinate;
import uk.ac.ox.poseidon.geography.bathymetry.BathymetricGrid;
import uk.ac.ox.poseidon.geography.grids.Cell;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;

/**
 * A grid of {@link Port}s placed on land cells, each adjacent to at least one
 * active water cell. Built via
 * {@link uk.ac.ox.poseidon.geography.ports.Factories}; implemented by
 * {@link ImmutablePortGrid} (fixed at build time) and {@link MutablePortGrid}
 * (ports can be added afterward).
 */
public interface PortGrid extends Iterable<Port> {
  /** @return every port on this grid */
  Stream<Port> getPorts();

  /** @return the cell {@code port} is placed on */
  Cell getCell(Port port);

  /** @return the ports placed on {@code cell}, usually at most one */
  Stream<Port> getObjectsAt(Cell cell);

  /** @return whether any port is placed on {@code cell} */
  boolean anyObjectsAt(Cell cell);

  /** @return the number of ports placed on {@code cell} */
  int numObjectsAt(Cell cell);

  /** @return the port with the given code, if one exists on this grid */
  Optional<Port> getObject(String id);

  /**
   * @throws IllegalStateException if {@code cell} isn't land, or has no active
   *                               water cell adjacent to it
   */
  default void validateCell(final Cell cell) {
    final Coordinate coordinate = getModelGrid().toCoordinate(cell);
    checkState(
      getBathymetricGrid().isLand(cell),
      "Invalid port cell: cell %s (centred on %s) is not on land.",
      cell,
      coordinate
    );
    checkState(
      getBathymetricGrid().getActiveWaterNeighbours(cell).findAny().isPresent(),
      "Invalid port cell: cell %s (centred on %s) has no active water " +
        "neighbour.",
      cell,
      coordinate
    );
  }

  /** @return the bathymetric grid ports are placed relative to */
  BathymetricGrid getBathymetricGrid();

  /** @return {@link #getBathymetricGrid()}'s underlying {@link ModelGrid} */
  default ModelGrid getModelGrid() {
    return getBathymetricGrid().getModelGrid();
  }
}
