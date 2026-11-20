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

import ec.util.MersenneTwisterFast;
import java.util.List;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.biology.species.Species;
import uk.ac.ox.poseidon.geography.Envelope;
import uk.ac.ox.poseidon.geography.grids.BaseDoubleGrid;
import uk.ac.ox.poseidon.geography.grids.Cell;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;

final class BiomassDiffuserTest {

  private static final Species COD = new Species("COD", null, "Cod");

  @Test
  void diffusesBetweenHabitableNeighboursAndLeavesOtherCellsAlone() {
    final ModelGrid modelGrid = ModelGrid.create(
      4,
      1,
      new Envelope(0, 4, 0, 1)
    ).withActiveCells(List.of(new Cell(0, 0), new Cell(1, 0), new Cell(2, 0)));
    final BiomassGrid biomassGrid = new DefaultBiomassGrid(
      modelGrid,
      COD,
      new double[][] { { 100 }, { 20 }, { 7 }, { 11 } }
    );
    final CarryingCapacityGrid capacityGrid = new CarryingCapacityGrid(
      new BaseDoubleGrid(modelGrid, new double[][] {
        { 200 },
        { 200 },
        { 0 },
        { 200 }
      })
    );
    final BiomassDiffuser diffuser = new BiomassDiffuser(
      biomassGrid,
      capacityGrid,
      new SmoothBiomassDiffusionRule(0.5, 1),
      new MersenneTwisterFast(1)
    );

    diffuser.step(null);

    assertThat(biomassGrid.getValue(new Cell(0, 0))).isEqualTo(60);
    assertThat(biomassGrid.getValue(new Cell(1, 0))).isEqualTo(60);
    assertThat(biomassGrid.getValue(new Cell(2, 0))).isEqualTo(7);
    assertThat(biomassGrid.getValue(new Cell(3, 0))).isEqualTo(11);
    assertThat(biomassGrid.getSum()).isEqualTo(138);
  }

  @Test
  void rejectsBiomassAndCapacityOnDifferentModelGrids() {
    final ModelGrid modelGrid = ModelGrid.create(
      1,
      1,
      new Envelope(0, 1, 0, 1)
    );
    final ModelGrid otherModelGrid = ModelGrid.create(
      2,
      1,
      new Envelope(0, 2, 0, 1)
    );
    final BiomassGrid biomassGrid = new DefaultBiomassGrid(modelGrid, COD, 10);
    final CarryingCapacityGrid capacityGrid = new CarryingCapacityGrid(
      new BaseDoubleGrid(otherModelGrid, 100)
    );

    assertThatIllegalArgumentException().isThrownBy(() ->
      new BiomassDiffuser(
        biomassGrid,
        capacityGrid,
        new SmoothBiomassDiffusionRule(0.5, 1),
        new MersenneTwisterFast(1)
      )
    );
  }
}
