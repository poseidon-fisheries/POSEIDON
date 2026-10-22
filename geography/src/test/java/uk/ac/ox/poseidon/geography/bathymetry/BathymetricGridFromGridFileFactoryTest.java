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

package uk.ac.ox.poseidon.geography.bathymetry;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.ox.poseidon.geography.bathymetry.Factories.bathymetricGridFromGridFile;
import static uk.ac.ox.poseidon.geography.grids.AscFiles.rowsFromNorth;
import static uk.ac.ox.poseidon.geography.grids.AscFiles.writeFixture;
import static uk.ac.ox.poseidon.geography.grids.Factories.modelGridFromGridFile;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import uk.ac.ox.poseidon.core.Scope;
import uk.ac.ox.poseidon.core.aggregators.Aggregator;
import uk.ac.ox.poseidon.core.aggregators.MaxAggregator;
import uk.ac.ox.poseidon.core.aggregators.MeanAggregator;
import uk.ac.ox.poseidon.geography.Envelope;
import uk.ac.ox.poseidon.geography.grids.Cell;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;

class BathymetricGridFromGridFileFactoryTest {

  @TempDir
  private Path tempDir;

  private BathymetricGrid read(
    final ModelGrid modelGrid,
    final Aggregator aggregator,
    final boolean inverted
  ) {
    final Path file = writeFixture(tempDir, "fixture.asc");
    return bathymetricGridFromGridFile(
      scope -> file,
      scope -> modelGrid,
      scope -> aggregator,
      inverted
    ).get(Scope.GLOBAL_SCOPE);
  }

  private ModelGrid modelGridFromTheFixture() {
    final Path file = writeFixture(tempDir, "model_grid.asc");
    return modelGridFromGridFile(scope -> file).get(Scope.GLOBAL_SCOPE);
  }

  private static double[][] elevations(
    final BathymetricGrid grid,
    final ModelGrid modelGrid
  ) {
    final double[][] rowsFromNorth =
      new double[modelGrid.getGridHeight()][modelGrid.getGridWidth()];
    modelGrid
      .getAllCells()
      .forEach(
        cell -> rowsFromNorth[cell.y()][cell.x()] = grid.getElevation(cell)
      );
    return rowsFromNorth;
  }

  @Test
  void onTheFilesOwnGridEachCellHasItsValue() {
    final ModelGrid modelGrid = modelGridFromTheFixture();
    final BathymetricGrid grid = read(modelGrid, new MaxAggregator(), false);
    assertThat(elevations(grid, modelGrid)).isDeepEqualTo(rowsFromNorth());
  }

  @Test
  void invertedNegatesTheValues() {
    final ModelGrid modelGrid = modelGridFromTheFixture();
    final BathymetricGrid grid = read(modelGrid, new MaxAggregator(), true);
    assertThat(elevations(grid, modelGrid)).isDeepEqualTo(new double[][] {
      { -1, -2, -3 },
      { -4, -5, -6 }
    });
    assertThat(grid.isWater(new Cell(0, 0))).isTrue();
  }

  @Test
  void onACoarserGridTheValuesInEachCellAreAggregated() {
    final ModelGrid oneCell = ModelGrid.create(
      1,
      1,
      new Envelope(10, 13, 40, 42)
    );
    assertThat(
      read(oneCell, new MaxAggregator(), false).getElevation(new Cell(0, 0))
    ).isEqualTo(6);
    assertThat(
      read(oneCell, new MeanAggregator(), false).getElevation(new Cell(0, 0))
    ).isEqualTo(3.5);
  }

  @Test
  void valuesGoToTheModelCellCoveringThemNotToTheSamePosition() {
    // One row per latitude band and one column per longitude band: a reading
    // that ignored the coordinates, or flipped north and south, would mix up
    // these maxima.
    final ModelGrid rows = ModelGrid.create(1, 2, new Envelope(10, 13, 40, 42));
    assertThat(
      elevations(read(rows, new MaxAggregator(), false), rows)
    ).isDeepEqualTo(new double[][] { { 3 }, { 6 } });
    final ModelGrid columns = ModelGrid.create(
      3,
      1,
      new Envelope(10, 13, 40, 42)
    );
    assertThat(
      elevations(read(columns, new MaxAggregator(), false), columns)
    ).isDeepEqualTo(new double[][] { { 4, 5, 6 } });
  }

  @Test
  void modelCellsBeyondTheFileAreZero() {
    // A fourth column, 13°E to 14°E, that the file does not cover
    final ModelGrid wider = ModelGrid.create(
      4,
      2,
      new Envelope(10, 14, 40, 42)
    );
    assertThat(
      elevations(read(wider, new MeanAggregator(), false), wider)
    ).isDeepEqualTo(new double[][] { { 1, 2, 3, 0 }, { 4, 5, 6, 0 } });
  }

  @Test
  void valuesWestOrNorthOfTheModelGridAreLeftOut() {
    // The model grid starts one column east and ends one row south of the file,
    // so the file's west column and north row lie outside it; they must not be
    // folded into the model's edge cells.
    final ModelGrid inner = ModelGrid.create(
      2,
      1,
      new Envelope(11, 13, 40, 41)
    );
    assertThat(
      elevations(read(inner, new MeanAggregator(), false), inner)
    ).isDeepEqualTo(new double[][] { { 5, 6 } });
  }
}
