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
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.geography.Envelope;
import uk.ac.ox.poseidon.geography.grids.Cell;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;

class CoordinateBasedDistanceCalculatorTest {

  private final ModelGrid modelGrid = ModelGrid.create(
    4,
    5,
    new Envelope(0, 4, 40, 45)
  );
  private final HaversineDistanceCalculator calculator =
    new HaversineDistanceCalculator(modelGrid);

  @Test
  void stepsBetweenNeighboursMatchTheFormula() {
    modelGrid.getAllCells().forEach(start -> {
      for (int dx = -1; dx <= 1; dx++) {
        for (int dy = -1; dy <= 1; dy++) {
          final Cell end = new Cell(start.x() + dx, start.y() + dy);
          if (modelGrid.isInGrid(end)) {
            assertThat(calculator.distanceInKm(start, end)).isCloseTo(
              calculator.distanceInKm(
                modelGrid.toCoordinate(start),
                modelGrid.toCoordinate(end)
              ),
              within(1e-9)
            );
          }
        }
      }
    });
  }

  @Test
  void stepsOutOfTheGridAreRefused() {
    assertThatThrownBy(() ->
      calculator.distanceInKm(new Cell(3, 2), new Cell(4, 2))
    ).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() ->
      calculator.distanceInKm(new Cell(-1, 2), new Cell(0, 2))
    ).isInstanceOf(IllegalArgumentException.class);
  }
}
