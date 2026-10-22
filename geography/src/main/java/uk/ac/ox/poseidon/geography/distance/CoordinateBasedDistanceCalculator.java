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

package uk.ac.ox.poseidon.geography.distance;

import static java.lang.Math.abs;

import java.util.function.ToDoubleBiFunction;
import sim.util.Int2D;
import uk.ac.ox.poseidon.geography.Coordinate;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;

/**
 * An {@link AbstractDistanceCalculator} that computes geographic distance from
 * lon/lat {@link uk.ac.ox.poseidon.geography.Coordinate}s: cells are
 * converted to the coordinates of their centres first, via
 * {@link ModelGrid#toCoordinate}.
 * Subclassed by {@link EquirectangularDistanceCalculator} and
 * {@link HaversineDistanceCalculator}, which supply the actual formula.
 * <p>
 * The length of a step between two neighbouring cells is computed once, when
 * the calculator is built, and looked up afterwards: on a regular lon/lat grid
 * it depends only on the starting row and the direction of the step.
 */
public abstract class CoordinateBasedDistanceCalculator
  extends AbstractDistanceCalculator
{

  /**
   * Indexed by {@link #stepIndex}; {@code NaN} for steps leaving the grid.
   */
  private final double[] stepLengthsInKm;
  private final ToDoubleBiFunction<Coordinate, Coordinate> formula;

  /**
   * @param modelGrid the grid this calculator measures distances over
   * @param formula   gives the distance in km between two coordinates
   */
  public CoordinateBasedDistanceCalculator(
    final ModelGrid modelGrid,
    final ToDoubleBiFunction<Coordinate, Coordinate> formula
  ) {
    super(modelGrid);
    this.formula = formula;
    final int gridHeight = modelGrid.getGridHeight();
    stepLengthsInKm = new double[gridHeight * 3 * 2];
    // A westward step is as long as the eastward one, so only steps from
    // column 0 to columns 0 and 1 are measured.
    for (int y = 0; y < gridHeight; y++) {
      final Int2D start = new Int2D(0, y);
      for (int dy = -1; dy <= 1; dy++) {
        for (int dx = 0; dx <= 1; dx++) {
          final Int2D end = new Int2D(dx, y + dy);
          stepLengthsInKm[stepIndex(y, dx, dy)] = modelGrid.isInGrid(end)
            ? formula.applyAsDouble(
                modelGrid.toCoordinate(start),
                modelGrid.toCoordinate(end)
              )
            : Double.NaN;
        }
      }
    }
  }

  private static int stepIndex(final int startY, final int dx, final int dy) {
    return (startY * 3 + dy + 1) * 2 + abs(dx);
  }

  @Override
  public final double distanceInKm(final Coordinate a, final Coordinate b) {
    return formula.applyAsDouble(a, b);
  }

  @Override
  public double distanceInKm(final Int2D start, final Int2D end) {
    if (modelGrid.isInGrid(start) && modelGrid.isInGrid(end)) {
      final int dx = end.x - start.x;
      final int dy = end.y - start.y;
      if (abs(dx) <= 1 && abs(dy) <= 1) {
        return stepLengthsInKm[stepIndex(start.y, dx, dy)];
      }
    }
    return distanceInKm(
      modelGrid.toCoordinate(start),
      modelGrid.toCoordinate(end)
    );
  }
}
