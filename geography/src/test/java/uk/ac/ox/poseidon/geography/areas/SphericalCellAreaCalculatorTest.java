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

import static java.lang.Math.PI;
import static java.lang.Math.sin;
import static java.lang.Math.toRadians;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.within;
import static uk.ac.ox.poseidon.geography.distance.AbstractDistanceCalculator.EARTH_RADIUS_IN_KM;

import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.geography.Envelope;
import uk.ac.ox.poseidon.geography.grids.Cell;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;

class SphericalCellAreaCalculatorTest {

  @Test
  void theCellsOfTheWholeSphereAddUpToItsArea() {
    final ModelGrid grid = ModelGrid.create(
      36,
      18,
      new Envelope(-180, 180, -90, 90)
    );
    final SphericalCellAreaCalculator calculator =
      new SphericalCellAreaCalculator(grid);

    final double total = grid
      .getAllCells()
      .mapToDouble(calculator::areaInSquareKm)
      .sum();

    assertThat(total).isCloseTo(
      4 * PI * EARTH_RADIUS_IN_KM * EARTH_RADIUS_IN_KM,
      within(1e-6 * total)
    );
  }

  @Test
  void aCellIsThePatchBetweenItsMeridiansAndParallels() {
    final ModelGrid grid = ModelGrid.create(2, 3, new Envelope(10, 12, 40, 43));
    final SphericalCellAreaCalculator calculator =
      new SphericalCellAreaCalculator(grid);

    assertThat(calculator.areaInSquareKm(new Cell(1, 2))).isCloseTo(
      EARTH_RADIUS_IN_KM *
        EARTH_RADIUS_IN_KM *
        toRadians(1) *
        (sin(toRadians(41)) - sin(toRadians(40))),
      within(1e-9)
    );
  }

  @Test
  void cellsOfARowHaveTheSameAreaWhichShrinksTowardsThePole() {
    final ModelGrid grid = ModelGrid.create(3, 2, new Envelope(0, 3, 50, 52));
    final SphericalCellAreaCalculator calculator =
      new SphericalCellAreaCalculator(grid);

    assertThat(calculator.areaInSquareKm(new Cell(0, 0))).isEqualTo(
      calculator.areaInSquareKm(new Cell(2, 0))
    );
    assertThat(calculator.areaInSquareKm(new Cell(0, 0))).isLessThan(
      calculator.areaInSquareKm(new Cell(0, 1))
    );
  }

  @Test
  void aCellOutsideTheGridIsRefused() {
    final SphericalCellAreaCalculator calculator =
      new SphericalCellAreaCalculator(
        ModelGrid.create(2, 2, new Envelope(0, 2, 0, 2))
      );

    assertThatIllegalArgumentException().isThrownBy(() ->
      calculator.areaInSquareKm(new Cell(0, 2))
    );
  }
}
