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

package uk.ac.ox.poseidon.geography.bathymetry;

import java.util.stream.Stream;
import uk.ac.ox.poseidon.geography.grids.Cell;
import uk.ac.ox.poseidon.geography.grids.DoubleGrid;

/**
 * A {@link DoubleGrid} whose values are elevations: negative for water (a
 * depth), non-negative for land. Built via
 * {@link uk.ac.ox.poseidon.geography.bathymetry.Factories}; implemented by
 * {@link DefaultBathymetricGrid}.
 */
public interface BathymetricGrid extends DoubleGrid {
  /**
   * @return every water cell that is also
   *         {@link uk.ac.ox.poseidon.geography.grids.ModelGrid#isActive}
   */
  default Stream<Cell> getActiveWaterCells() {
    return getWaterCells().filter(getModelGrid()::isActive);
  }

  /**
   * @return {@code cell}'s active water neighbours, within a neighbourhood size
   *         of 1
   */
  default Stream<Cell> getActiveWaterNeighbours(final Cell cell) {
    return getActiveWaterNeighbours(cell, 1);
  }

  /**
   * @return {@code cell}'s active water neighbours, within the given
   *         neighbourhood size
   */
  default Stream<Cell> getActiveWaterNeighbours(
    final Cell cell,
    final int neighbourhoodSize
  ) {
    return getModelGrid()
      .getActiveNeighbours(cell, neighbourhoodSize)
      .stream()
      .filter(this::isWater);
  }

  /**
   * @return {@code cell}'s water neighbours, within a neighbourhood size of 1
   */
  default Stream<Cell> getWaterNeighbours(final Cell cell) {
    return getWaterNeighbours(cell, 1);
  }

  /**
   * @return {@code cell}'s water neighbours, within the given neighbourhood
   *         size
   */
  default Stream<Cell> getWaterNeighbours(
    final Cell cell,
    final int neighbourhoodSize
  ) {
    return getModelGrid()
      .getNeighbours(cell, neighbourhoodSize)
      .stream()
      .filter(this::isWater);
  }

  /**
   * @return {@code cell}'s land neighbours, within a neighbourhood size of 1
   */
  default Stream<Cell> getLandNeighbours(final Cell cell) {
    return getLandNeighbours(cell, 1);
  }

  /**
   * @return {@code cell}'s land neighbours, within the given neighbourhood size
   */
  default Stream<Cell> getLandNeighbours(
    final Cell cell,
    final int neighbourhoodSize
  ) {
    return getModelGrid()
      .getNeighbours(cell, neighbourhoodSize)
      .stream()
      .filter(this::isLand);
  }

  /**
   * @return {@code cell}'s land neighbours that are also active, within a
   *         neighbourhood size of 1
   */
  default Stream<Cell> getActiveLandNeighbours(final Cell cell) {
    return getActiveLandNeighbours(cell, 1);
  }

  /**
   * @return {@code cell}'s land neighbours that are also active, within the
   *         given neighbourhood size
   */
  default Stream<Cell> getActiveLandNeighbours(
    final Cell cell,
    final int neighbourhoodSize
  ) {
    return getModelGrid()
      .getActiveNeighbours(cell, neighbourhoodSize)
      .stream()
      .filter(this::isLand);
  }

  /** @return every water cell in the grid */
  default Stream<Cell> getWaterCells() {
    return getModelGrid().getAllCells().filter(this::isWater);
  }

  /** @return every land cell in the grid */
  default Stream<Cell> getLandCells() {
    return getModelGrid().getAllCells().filter(this::isLand);
  }

  /** @return every cell in the grid, land and water */
  default Stream<Cell> getAllCells() {
    return getModelGrid().getAllCells();
  }

  /** @return whether {@code cell} is land (not water) */
  default boolean isLand(final Cell cell) {
    return !isWater(cell);
  }

  /** @return whether {@code cell}'s elevation is negative */
  default boolean isWater(final Cell cell) {
    return getElevation(cell) < 0;
  }

  /**
   * @return {@code cell}'s elevation: negative for water, non-negative for land
   */
  double getElevation(final Cell cell);

  /**
   * @return the negation of {@link #getElevation(Cell)}, positive for water
   */
  default double getDepth(final Cell cell) {
    return -getElevation(cell);
  }

  /**
   * @return whether {@code cell} is both water and
   *         {@link uk.ac.ox.poseidon.geography.grids.ModelGrid#isActive}
   */
  default boolean isActiveWater(final Cell cell) {
    return isWater(cell) && getModelGrid().isActive(cell);
  }
}
