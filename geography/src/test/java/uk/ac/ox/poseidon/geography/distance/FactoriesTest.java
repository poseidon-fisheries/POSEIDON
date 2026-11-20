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
import static uk.ac.ox.poseidon.core.Scope.GLOBAL_SCOPE;
import static uk.ac.ox.poseidon.geography.distance.Factories.cartesianDistanceCalculator;
import static uk.ac.ox.poseidon.geography.distance.Factories.equirectangularDistanceCalculator;
import static uk.ac.ox.poseidon.geography.distance.Factories.haversineDistanceCalculator;

import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.geography.Envelope;
import uk.ac.ox.poseidon.geography.grids.Cell;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;

final class FactoriesTest {

  private final ModelGrid grid = ModelGrid.create(
    2,
    2,
    new Envelope(0, 2, 0, 2)
  );
  private final Cell start = new Cell(0, 0);
  private final Cell end = new Cell(1, 0);

  @Test
  void cartesianCalculatorScalesGridDistanceByTheCellSize() {
    final DistanceCalculator calculator = cartesianDistanceCalculator(
      ignored -> grid,
      7.5
    ).get(GLOBAL_SCOPE);

    assertThat(calculator).isInstanceOf(CartesianDistanceCalculator.class);
    assertThat(calculator.distanceInKm(start, end)).isEqualTo(7.5);
  }

  @Test
  void haversineCalculatorMeasuresOneDegreeOfLongitudeAtTheEquator() {
    final DistanceCalculator calculator = haversineDistanceCalculator(
      ignored -> grid
    ).get(GLOBAL_SCOPE);

    assertThat(calculator).isInstanceOf(HaversineDistanceCalculator.class);
    assertThat(calculator.distanceInKm(start, end)).isGreaterThan(100);
  }

  @Test
  void equirectangularCalculatorMeasuresOneDegreeOfLongitudeAtTheEquator() {
    final DistanceCalculator calculator = equirectangularDistanceCalculator(
      ignored -> grid
    ).get(GLOBAL_SCOPE);

    assertThat(calculator).isInstanceOf(
      EquirectangularDistanceCalculator.class
    );
    assertThat(calculator.distanceInKm(start, end)).isGreaterThan(100);
  }
}
