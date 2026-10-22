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

package uk.ac.ox.poseidon.geography.grids;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.ox.poseidon.geography.grids.AscFiles.writeFixture;
import static uk.ac.ox.poseidon.geography.grids.Factories.modelGridFromGridFile;

import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import uk.ac.ox.poseidon.core.Scope;
import uk.ac.ox.poseidon.geography.Coordinate;
import uk.ac.ox.poseidon.geography.Envelope;

class ModelGridFromGridFileFactoryTest {

  @TempDir
  private Path tempDir;

  private ModelGrid modelGrid;

  @BeforeEach
  void setUp() {
    final Path file = writeFixture(tempDir, "fixture.asc");
    modelGrid = modelGridFromGridFile(scope -> file).get(Scope.GLOBAL_SCOPE);
  }

  @Test
  void gridHasTheFilesSizeAndExtent() {
    assertThat(modelGrid.getGridWidth()).isEqualTo(3);
    assertThat(modelGrid.getGridHeight()).isEqualTo(2);
    assertThat(modelGrid.getEnvelope()).isEqualTo(new Envelope(10, 13, 40, 42));
  }

  @Test
  void cellsAreCentredOnTheFilesCellsWithRowZeroInTheNorth() {
    assertThat(modelGrid.toCoordinate(new Cell(0, 0))).isEqualTo(
      new Coordinate(10.5, 41.5)
    );
    assertThat(modelGrid.toCoordinate(new Cell(2, 0))).isEqualTo(
      new Coordinate(12.5, 41.5)
    );
    assertThat(modelGrid.toCoordinate(new Cell(0, 1))).isEqualTo(
      new Coordinate(10.5, 40.5)
    );
    assertThat(modelGrid.toCoordinate(new Cell(2, 1))).isEqualTo(
      new Coordinate(12.5, 40.5)
    );
  }

  @Test
  void coordinatesAreInTheCellCoveringThem() {
    assertThat(modelGrid.toCell(new Coordinate(10.1, 41.9))).isEqualTo(
      new Cell(0, 0)
    );
    assertThat(modelGrid.toCell(new Coordinate(12.9, 40.1))).isEqualTo(
      new Cell(2, 1)
    );
    assertThat(modelGrid.toCell(new Coordinate(11.5, 40.5))).isEqualTo(
      new Cell(1, 1)
    );
  }

  @Test
  void everyCellIsActive() {
    assertThat(modelGrid.getAllCells().allMatch(modelGrid::isActive)).isTrue();
    assertThat(modelGrid.getAllCells()).hasSize(6);
  }

  @Test
  void coordinatesJustWestOrNorthOfTheGridAreOutsideIt() {
    assertThat(modelGrid.toCell(new Coordinate(9.9, 41.5))).isEqualTo(
      new Cell(-1, 0)
    );
    assertThat(modelGrid.toCell(new Coordinate(10.5, 42.1))).isEqualTo(
      new Cell(0, -1)
    );
    assertThat(modelGrid.isInGrid(new Coordinate(9.9, 41.5))).isFalse();
    assertThat(modelGrid.isInGrid(new Coordinate(10.5, 42.1))).isFalse();
    assertThat(modelGrid.isInGrid(new Coordinate(13.1, 40.5))).isFalse();
    assertThat(modelGrid.isInGrid(new Coordinate(10.5, 39.9))).isFalse();
  }
}
