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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import sim.util.Double2D;
import uk.ac.ox.poseidon.geography.Coordinate;
import uk.ac.ox.poseidon.geography.grids.Cell;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;

class CartesianDistanceCalculatorTest {

  private final CartesianDistanceCalculator calculator =
    new CartesianDistanceCalculator(null, 2.0);

  @Test
  void threeFourFiveTriangle() {
    // 3-4-5 triangle scaled by cellSizeInKm=2 should give a 6-8-10 result.
    assertThat(
      calculator.distanceInKm(new Cell(0, 0), new Cell(3, 4))
    ).isCloseTo(10.0, within(1e-9));
  }

  @Test
  void samePointIsZero() {
    final Cell cell = new Cell(5, 5);
    assertThat(calculator.distanceInKm(cell, cell)).isCloseTo(
      0.0,
      within(1e-9)
    );
  }

  @Test
  void isSymmetric() {
    final Cell a = new Cell(1, 2);
    final Cell b = new Cell(7, -3);
    assertThat(calculator.distanceInKm(a, b)).isCloseTo(
      calculator.distanceInKm(b, a),
      within(1e-9)
    );
  }

  @Test
  void distanceBetweenCellsDoesNotDependOnTheirPosition() {
    assertThat(
      calculator.distanceInKm(new Cell(1, 1), new Cell(4, 5))
    ).isCloseTo(10.0, within(1e-9));
  }

  @Test
  void distanceBetweenCoordinatesIsMeasuredBetweenTheirGridPoints() {
    final ModelGrid modelGrid = mock(ModelGrid.class);
    final Coordinate a = new Coordinate(1, 1);
    final Coordinate b = new Coordinate(2, 2);
    when(modelGrid.toPoint(a)).thenReturn(new Double2D(1, 1));
    when(modelGrid.toPoint(b)).thenReturn(new Double2D(4, 5));

    assertThat(
      new CartesianDistanceCalculator(modelGrid, 2.0).distanceInKm(a, b)
    ).isCloseTo(10.0, within(1e-9));
  }
}
