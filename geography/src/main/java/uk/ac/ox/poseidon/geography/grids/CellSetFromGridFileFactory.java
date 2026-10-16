/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025-2026, University of Oxford.
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

import static com.google.common.base.Preconditions.checkArgument;
import static lombok.AccessLevel.PACKAGE;

import com.google.common.collect.ImmutableSet;
import java.io.File;
import java.nio.file.Path;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import sim.util.Int2D;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.RelativeScopeFactory;
import uk.ac.ox.poseidon.core.scopes.Scope;
import uk.ac.ox.poseidon.geography.Envelope;

/**
 * A {@link RelativeScopeFactory} that reads a raster grid file and returns the
 * set of cells whose value equals a fixed target — e.g. picking out land cells
 * from a land/water raster. There's no separate plain component class here: the
 * produced {@link ImmutableSet} is returned as-is, with no wrapper type to
 * carry documentation, so this factory carries the behavior doc directly.
 * <p>
 * The file's columns and rows are taken as the cells of {@code modelGrid}, so
 * the file must cover the same cells: the same number of columns and rows, and
 * the same extent to within a hundredth of a cell (enough to allow for the same
 * header printed with different precision). Built via
 * {@link Factories#cellSetFromGridFile(Factory, Factory, double)} in this
 * package.
 */
@Getter
@ToString
@AllArgsConstructor(access = PACKAGE)
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
public class CellSetFromGridFileFactory<S extends Scope> extends RelativeScopeFactory<
  S,
  ImmutableSet<Int2D>
> {

  /**
   * The share of a cell by which the file's extent may differ from the model
   * grid's.
   */
  private static final double EXTENT_TOLERANCE_IN_CELLS = 0.01;

  private final Factory<? super S, ? extends Path> path;
  private final Factory<? super S, ? extends ModelGrid> modelGrid;
  private final double includedValue;

  /**
   * @throws RuntimeException         if {@code path} doesn't point to a file
   * @throws IllegalArgumentException if the file doesn't cover the same cells
   *                                  as {@code modelGrid}
   */
  @Override
  protected ImmutableSet<Int2D> newInstance(final S scope) {
    final File gridFile = path.get(scope).toFile();
    if (!gridFile.isFile()) {
      throw new RuntimeException(gridFile + " does not point to a file.");
    }
    final CoverageWrapper coverageWrapper = new CoverageWrapper(gridFile);
    checkSameCells(gridFile, coverageWrapper, modelGrid.get(scope));
    final ImmutableSet.Builder<Int2D> builder = ImmutableSet.builder();
    coverageWrapper.processGrid((cell, value) -> {
      if (value == includedValue) {
        builder.add(cell);
      }
    });
    return builder.build();
  }

  private static void checkSameCells(
    final File gridFile,
    final CoverageWrapper coverageWrapper,
    final ModelGrid modelGrid
  ) {
    checkArgument(
      coverageWrapper.getGridWidth() == modelGrid.getGridWidth() &&
        coverageWrapper.getGridHeight() == modelGrid.getGridHeight(),
      "%s has %s columns and %s rows, but the model grid has %s and %s.",
      gridFile,
      coverageWrapper.getGridWidth(),
      coverageWrapper.getGridHeight(),
      modelGrid.getGridWidth(),
      modelGrid.getGridHeight()
    );
    final Envelope fileExtent = coverageWrapper.makeEnvelope();
    final Envelope gridExtent = modelGrid.getEnvelope();
    final double xTolerance =
      EXTENT_TOLERANCE_IN_CELLS * modelGrid.getCellWidth();
    final double yTolerance =
      EXTENT_TOLERANCE_IN_CELLS * modelGrid.getCellHeight();
    checkArgument(
      Math.abs(fileExtent.getMinX() - gridExtent.getMinX()) <= xTolerance &&
        Math.abs(fileExtent.getMaxX() - gridExtent.getMaxX()) <= xTolerance &&
        Math.abs(fileExtent.getMinY() - gridExtent.getMinY()) <= yTolerance &&
        Math.abs(fileExtent.getMaxY() - gridExtent.getMaxY()) <= yTolerance,
      "%s covers %s, but the model grid covers %s.",
      gridFile,
      fileExtent,
      gridExtent
    );
  }
}
