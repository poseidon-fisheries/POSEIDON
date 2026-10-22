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

package uk.ac.ox.poseidon.geography.distance;

import sim.util.Double2D;
import sim.util.Int2D;
import uk.ac.ox.poseidon.geography.Coordinate;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;

/**
 * An {@link AbstractDistanceCalculator} that measures straight-line
 * (Pythagorean) distance in grid-cell units, scaled by a fixed cell size,
 * rather than computing geographic distance from lon/lat coordinates:
 * coordinates are converted to grid points first, via
 * {@link ModelGrid#toPoint}. Cheaper than
 * {@link CoordinateBasedDistanceCalculator}'s implementations, at the cost of
 * assuming a uniform cell size across the grid. Built via
 * {@link Factories#cartesianDistanceCalculator(uk.ac.ox.poseidon.core.Factory, double)}
 * in this package.
 */
public class CartesianDistanceCalculator extends AbstractDistanceCalculator {

  private final double cellSizeInKm;

  /**
   * @param modelGrid    the grid to measure distances over
   * @param cellSizeInKm the length, in kilometres, of one grid cell's side
   */
  public CartesianDistanceCalculator(
    final ModelGrid modelGrid,
    final double cellSizeInKm
  ) {
    super(modelGrid);
    this.cellSizeInKm = cellSizeInKm;
  }

  @Override
  public double distanceInKm(final Coordinate a, final Coordinate b) {
    final Double2D pointA = modelGrid.toPoint(a);
    final Double2D pointB = modelGrid.toPoint(b);
    return cellSizeInKm * Math.hypot(pointB.x - pointA.x, pointB.y - pointA.y);
  }

  @Override
  public double distanceInKm(final Int2D start, final Int2D end) {
    return cellSizeInKm * Math.hypot(end.x - start.x, end.y - start.y);
  }
}
