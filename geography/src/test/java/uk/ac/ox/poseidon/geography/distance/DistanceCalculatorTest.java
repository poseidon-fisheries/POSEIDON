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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static tech.units.indriya.quantity.Quantities.getQuantity;
import static tech.units.indriya.unit.Units.KILOMETRE_PER_HOUR;
import static tech.units.indriya.unit.Units.METRE_PER_SECOND;

import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;
import sim.util.Double2D;
import uk.ac.ox.poseidon.geography.Coordinate;
import uk.ac.ox.poseidon.geography.grids.Cell;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;

class DistanceCalculatorTest {

  @Test
  void travelDurationIsDistanceOverSpeed() {
    assertThat(DistanceCalculator.travelDuration(5.0, 10.0)).isEqualTo(
      Duration.ofMinutes(30)
    );
  }

  @Test
  void travelDurationRoundsUpToTheNextWholeSecond() {
    assertThat(DistanceCalculator.travelDuration(0.001, 10.0)).isEqualTo(
      Duration.ofSeconds(1)
    );
    assertThat(DistanceCalculator.travelDuration(0.0030001, 10.0)).isEqualTo(
      Duration.ofSeconds(2)
    );
  }

  @Test
  void travelDurationOfAWholeNumberOfSecondsIsUnchanged() {
    assertThat(DistanceCalculator.travelDuration(10.0, 10.0)).isEqualTo(
      Duration.ofHours(1)
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

  @Test
  void distanceBetweenCellsIsALengthInKilometres() {
    final DistanceCalculator calculator = new CartesianDistanceCalculator(
      null,
      2.0
    );
    final var distance = calculator.distance(new Cell(0, 0), new Cell(3, 4));
    assertThat(distance.getValue().doubleValue()).isEqualTo(10.0);
    assertThat(distance.getUnit().toString()).isEqualTo("km");
  }

  @Test
  void distanceBetweenCoordinatesIsALengthInKilometres() {
    final ModelGrid grid = mock(ModelGrid.class);
    final Coordinate a = new Coordinate(0, 0);
    final Coordinate b = new Coordinate(1, 1);
    when(grid.toPoint(a)).thenReturn(new Double2D(0, 0));
    when(grid.toPoint(b)).thenReturn(new Double2D(3, 4));
    final DistanceCalculator calculator = new CartesianDistanceCalculator(
      grid,
      2.0
    );
    final var distance = calculator.distance(a, b);
    assertThat(distance.getValue().doubleValue()).isEqualTo(10.0);
    assertThat(distance.getUnit().toString()).isEqualTo("km");
  }

  @Test
  void travelDurationBetweenCellsAtASpeedInKphIsDistanceOverSpeed() {
    final DistanceCalculator calculator = new CartesianDistanceCalculator(
      null,
      1.0
    );
    assertThat(
      calculator.travelDuration(new Cell(0, 0), new Cell(3, 4), 10.0)
    ).isEqualTo(Duration.ofMinutes(30));
  }

  @Test
  void travelDurationBetweenCellsAtAQuantitySpeedIsDistanceOverSpeed() {
    final DistanceCalculator calculator = new CartesianDistanceCalculator(
      null,
      1.0
    );
    assertThat(
      calculator.travelDuration(
        new Cell(0, 0),
        new Cell(3, 4),
        getQuantity(10.0, KILOMETRE_PER_HOUR)
      )
    ).isEqualTo(Duration.ofMinutes(30));
  }

  @Test
  void travelDurationAlongAPathConvertsAQuantitySpeedToKph() {
    final DistanceCalculator calculator = new CartesianDistanceCalculator(
      null,
      1.0
    );
    assertThat(
      calculator.travelDuration(
        List.of(new Cell(0, 0), new Cell(3, 4)),
        getQuantity(5.0, METRE_PER_SECOND)
      )
    ).isEqualTo(Duration.ofMinutes(16).plusSeconds(40));
  }
}
