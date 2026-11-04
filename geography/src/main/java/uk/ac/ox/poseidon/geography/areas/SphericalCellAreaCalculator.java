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

package uk.ac.ox.poseidon.geography.areas;

import static java.lang.Math.sin;
import static java.lang.Math.toRadians;
import static uk.ac.ox.poseidon.geography.distance.AbstractDistanceCalculator.EARTH_RADIUS_IN_KM;

import java.util.stream.IntStream;
import lombok.Getter;
import lombok.NonNull;
import uk.ac.ox.poseidon.geography.grids.Cell;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;

/**
 * A {@link CellAreaCalculator} for a grid in degrees of longitude and latitude,
 * measuring each cell as the patch of a sphere of radius
 * {@link uk.ac.ox.poseidon.geography.distance.AbstractDistanceCalculator#EARTH_RADIUS_IN_KM}
 * between its meridians and parallels: {@code R² × Δλ × (sin φ₁ − sin φ₂)},
 * with {@code Δλ} the cell's width in radians, and {@code φ₁} and {@code φ₂}
 * the latitudes of its northern and southern edges. The cells of a row all
 * have the same area, which shrinks towards the poles. The areas are worked
 * out once per row, when the calculator is built. Built via
 * {@link Factories#sphericalCellAreaCalculator(uk.ac.ox.poseidon.core.Factory)}.
 */
public class SphericalCellAreaCalculator implements CellAreaCalculator {

  /** The grid this calculator measures the cells of. */
  @Getter
  private final ModelGrid modelGrid;

  /** The area of the cells of each row, indexed by {@code y}, in km². */
  private final double[] rowAreasInSquareKm;

  /** @param modelGrid the grid to measure the cells of */
  public SphericalCellAreaCalculator(@NonNull final ModelGrid modelGrid) {
    this.modelGrid = modelGrid;
    final double north = modelGrid.getEnvelope().getMaxY();
    final double cellHeight = modelGrid.getCellHeight();
    final double widthInRadians = toRadians(modelGrid.getCellWidth());
    this.rowAreasInSquareKm = IntStream.range(0, modelGrid.getGridHeight())
      .mapToDouble(
        y ->
          EARTH_RADIUS_IN_KM *
          EARTH_RADIUS_IN_KM *
          widthInRadians *
          (sin(toRadians(north - y * cellHeight)) -
            sin(toRadians(north - (y + 1) * cellHeight)))
      )
      .toArray();
  }

  /**
   * @throws IllegalArgumentException if {@code cell} is outside the grid
   */
  @Override
  public double areaInSquareKm(final Cell cell) {
    modelGrid.checkIsInGrid(cell);
    return rowAreasInSquareKm[cell.y()];
  }
}
