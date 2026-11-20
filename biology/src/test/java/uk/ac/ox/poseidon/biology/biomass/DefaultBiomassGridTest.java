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

package uk.ac.ox.poseidon.biology.biomass;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.biology.Fisheable;
import uk.ac.ox.poseidon.biology.buckets.Bucket;
import uk.ac.ox.poseidon.biology.species.Species;
import uk.ac.ox.poseidon.geography.Envelope;
import uk.ac.ox.poseidon.geography.grids.Cell;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;

final class DefaultBiomassGridTest {

  private static final Species COD = new Species("COD", null, "Cod");
  private static final Cell CELL = new Cell(0, 0);
  private final ModelGrid modelGrid = ModelGrid.create(
    2,
    2,
    new Envelope(0, 2, 0, 2)
  );

  @Test
  void sumIncludesAllRowsAndColumnsAndIgnoresExcludedCells() {
    final BiomassGrid grid = new DefaultBiomassGrid(
      modelGrid,
      COD,
      new double[][] { { 2, Double.NaN }, { 5, 7 } }
    );

    assertThat(grid.getSum()).isEqualTo(14);
  }

  @Test
  void sumOfAnEntirelyExcludedGridIsZero() {
    final BiomassGrid grid = new DefaultBiomassGrid(modelGrid, COD, Double.NaN);

    assertThat(grid.getSum()).isZero();
  }

  @Test
  void releasedBiomassBecomesAvailableAtTheSelectedCell() {
    final BiomassGrid grid = new DefaultBiomassGrid(modelGrid, COD, 8);
    final Fisheable cell = grid.getFisheableCell(CELL);

    cell.release(Bucket.of(COD, 3));

    assertThat(cell.availableFish().getKg(COD)).isEqualTo(11);
    assertThat(cell.availableFish().getSpecies()).containsExactly(COD);
    assertThat(grid.getValue(CELL)).isEqualTo(11);
    assertThat(grid.getValue(new Cell(1, 0))).isEqualTo(8);
  }

  @Test
  void releaseRejectsAnotherSpeciesWithoutChangingBiomass() {
    final BiomassGrid grid = new DefaultBiomassGrid(modelGrid, COD, 8);
    final Species haddock = new Species("HAD", null, "Haddock");

    assertThatIllegalArgumentException().isThrownBy(() ->
      grid.getFisheableCell(CELL).release(Bucket.of(haddock, 3))
    );
    assertThat(grid.getValue(CELL)).isEqualTo(8);
  }

  @Test
  void quantitySetterUpdatesBiomassAtTheSelectedCell() {
    final BiomassGrid grid = new DefaultBiomassGrid(modelGrid, COD);

    grid.setBiomass(CELL, Biomass.ofKg(12));

    assertThat(grid.getBiomass(CELL).asKg()).isEqualTo(12);
    assertThat(grid.getValue(new Cell(1, 0))).isZero();
  }
}
