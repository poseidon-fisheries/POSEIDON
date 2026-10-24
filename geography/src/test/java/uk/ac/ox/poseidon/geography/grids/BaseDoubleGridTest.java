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

package uk.ac.ox.poseidon.geography.grids;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.geography.Coordinate;
import uk.ac.ox.poseidon.geography.Envelope;

class BaseDoubleGridTest {

  private final BaseDoubleGrid grid = new BaseDoubleGrid(new double[][] {
    { 1d, 2d, 3d },
    { 4d, 5d, 6d },
    { 7d, 8d, 9d }
  });

  @Test
  void getValue() {
    assertThat(grid.getValue(new Cell(0, 0))).isEqualTo(1.0);
    assertThat(grid.getValue(new Cell(0, 1))).isEqualTo(2.0);
    assertThat(grid.getValue(new Cell(1, 0))).isEqualTo(4.0);
    assertThat(grid.getValue(new Cell(2, 2))).isEqualTo(9.0);
  }

  @Test
  void getMinimumValue() {
    assertThat(grid.getMinimumValue()).isEqualTo(1.0);
  }

  @Test
  void getMaximumValue() {
    assertThat(grid.getMaximumValue()).isEqualTo(9.0);
  }

  @Test
  void testModelGrid() {
    final ModelGrid modelGrid = grid.getModelGrid();
    assertThat(modelGrid.getGridWidth()).isEqualTo(3);
    assertThat(modelGrid.getGridHeight()).isEqualTo(3);
    assertThat(
      modelGrid.getAllCells().map(modelGrid::toCoordinate).toList()
    ).isEqualTo(
      List.of(
        new Coordinate(0.5, 2.5),
        new Coordinate(0.5, 1.5),
        new Coordinate(0.5, 0.5),
        new Coordinate(1.5, 2.5),
        new Coordinate(1.5, 1.5),
        new Coordinate(1.5, 0.5),
        new Coordinate(2.5, 2.5),
        new Coordinate(2.5, 1.5),
        new Coordinate(2.5, 0.5)
      )
    );
  }

  @Test
  void testEnvelope() {
    final Envelope envelope = grid.getModelGrid().getEnvelope();
    assertThat(envelope.getWidth()).isEqualTo(3);
    assertThat(envelope.getHeight()).isEqualTo(3);
  }
}
