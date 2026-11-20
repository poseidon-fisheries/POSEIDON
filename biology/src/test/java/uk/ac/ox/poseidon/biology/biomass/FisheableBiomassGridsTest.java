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
import static uk.ac.ox.poseidon.biology.biomass.Factories.fisheableBiomassGrids;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.biology.Fisheable;
import uk.ac.ox.poseidon.biology.buckets.Bucket;
import uk.ac.ox.poseidon.biology.species.Species;
import uk.ac.ox.poseidon.core.Scope;
import uk.ac.ox.poseidon.geography.Envelope;
import uk.ac.ox.poseidon.geography.grids.Cell;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;

final class FisheableBiomassGridsTest {

  private static final Species COD = new Species("COD", null, "Cod");
  private static final Species HAD = new Species("HAD", null, "Haddock");
  private static final Species OTHER = new Species("OTHER", null, "Other");
  private static final Cell CELL = new Cell(0, 0);
  private static final Cell NEIGHBOUR = new Cell(1, 0);
  private final ModelGrid modelGrid = ModelGrid.create(
    2,
    1,
    new Envelope(0, 2, 0, 1)
  );
  private final BiomassGrid codGrid = new DefaultBiomassGrid(
    modelGrid,
    COD,
    10
  );
  private final BiomassGrid haddockGrid = new DefaultBiomassGrid(
    modelGrid,
    HAD,
    20
  );
  private final FisheableBiomassGrids grids = fisheableBiomassGrids(scope ->
    List.of(haddockGrid, codGrid)
  ).get(Scope.GLOBAL_SCOPE);

  @Test
  void extractionCapsEachSpeciesAndUpdatesOnlyTheSelectedCell() {
    final Fisheable cell = grids.getFisheableCell(CELL);

    final Bucket extracted = cell.extract(
      Bucket.of(Map.of(COD, Biomass.ofKg(15), HAD, Biomass.ofKg(7)))
    );

    assertThat(extracted.getKg(COD)).isEqualTo(10);
    assertThat(extracted.getKg(HAD)).isEqualTo(7);
    assertThat(cell.availableFish().getKg(COD)).isZero();
    assertThat(cell.availableFish().getKg(HAD)).isEqualTo(13);
    assertThat(codGrid.getValue(NEIGHBOUR)).isEqualTo(10);
    assertThat(haddockGrid.getValue(NEIGHBOUR)).isEqualTo(20);
  }

  @Test
  void releaseAddsEachSpeciesToItsBackingGrid() {
    final Fisheable cell = grids.getFisheableCell(CELL);

    cell.release(Bucket.of(Map.of(COD, Biomass.ofKg(3), HAD, Biomass.ofKg(5))));

    assertThat(codGrid.getValue(CELL)).isEqualTo(13);
    assertThat(haddockGrid.getValue(CELL)).isEqualTo(25);
    assertThat(codGrid.getValue(NEIGHBOUR)).isEqualTo(10);
    assertThat(haddockGrid.getValue(NEIGHBOUR)).isEqualTo(20);
  }

  @Test
  void releaseRejectsSpeciesWithoutABackingGrid() {
    assertThatIllegalArgumentException().isThrownBy(() ->
      grids.getFisheableCell(CELL).release(Bucket.of(OTHER, 4))
    );
    assertThat(codGrid.getValue(CELL)).isEqualTo(10);
    assertThat(haddockGrid.getValue(CELL)).isEqualTo(20);
  }

  @Test
  void mappingBuildsNewGridsWithTheSameSpeciesIndex() {
    final FisheableBiomassGrids mapped = grids.mapValue(grid ->
      new DefaultBiomassGrid(modelGrid, grid.getSpecies(), 7)
    );

    assertThat(mapped.getSpeciesIndex()).isSameAs(grids.getSpeciesIndex());
    assertThat(mapped.get(COD).getSpecies()).isEqualTo(COD);
    assertThat(mapped.get(HAD).getSpecies()).isEqualTo(HAD);
    assertThat(mapped.get(COD).getValue(CELL)).isEqualTo(7);
    assertThat(mapped.get(HAD).getValue(CELL)).isEqualTo(7);
    assertThat(codGrid.getValue(CELL)).isEqualTo(10);
    assertThat(haddockGrid.getValue(CELL)).isEqualTo(20);
  }

  @Test
  void newInstanceRejectsAnArrayWithTheWrongSpeciesCount() {
    assertThatIllegalArgumentException()
      .isThrownBy(() ->
        Objects.requireNonNull(grids.newInstance(new BiomassGrid[] { codGrid }))
      )
      .withMessage("Grid array length must match species index size");
  }

  @Test
  void duplicateSpeciesAreRejected() {
    assertThatIllegalArgumentException().isThrownBy(() ->
      new FisheableBiomassGrids(List.of(codGrid, codGrid))
    );
  }
}
