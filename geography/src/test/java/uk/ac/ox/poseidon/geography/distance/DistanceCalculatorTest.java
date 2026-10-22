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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.geography.grids.Cell;

class DistanceCalculatorTest {

  @Test
  void travelDurationIsDistanceOverSpeed() {
    assertThat(DistanceCalculator.travelDuration(5.0, 10.0)).isEqualTo(
      Duration.ofMinutes(30)
    );
  }

  @Test
  void travelDurationAlongAPathIsItsLengthOverSpeed() {
    final DistanceCalculator calculator = new CartesianDistanceCalculator(
      null,
      1.0
    );
    assertThat(
      calculator.travelDuration(List.of(new Cell(0, 0), new Cell(3, 4)), 10.0)
    ).isEqualTo(Duration.ofMinutes(30));
  }

  @Test
  void travelDurationAlongAOneCellPathIsZero() {
    final DistanceCalculator calculator = new CartesianDistanceCalculator(
      null,
      1.0
    );
    assertThat(
      calculator.travelDuration(List.of(new Cell(3, 4)), 10.0)
    ).isEqualTo(Duration.ZERO);
  }

  @Test
  void travelDurationAlongAnEmptyPathIsRefused() {
    final DistanceCalculator calculator = new CartesianDistanceCalculator(
      null,
      1.0
    );
    assertThatThrownBy(() ->
      calculator.travelDuration(List.of(), 10.0)
    ).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void distanceAlongAPathSumsItsLegs() {
    final DistanceCalculator calculator = new CartesianDistanceCalculator(
      null,
      1.0
    );
    assertThat(
      calculator.distanceInKm(
        List.of(new Cell(0, 0), new Cell(3, 4), new Cell(3, 8))
      )
    ).isEqualTo(9.0);
  }

  @Test
  void distanceAlongAOneCellPathIsZero() {
    final DistanceCalculator calculator = new CartesianDistanceCalculator(
      null,
      1.0
    );
    assertThat(calculator.distanceInKm(List.of(new Cell(3, 4)))).isEqualTo(0.0);
  }
}
