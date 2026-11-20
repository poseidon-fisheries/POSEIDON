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
import static uk.ac.ox.poseidon.core.Scope.GLOBAL_SCOPE;
import static uk.ac.ox.poseidon.geography.bathymetry.Factories.roughCoastalBathymetricGrid;
import static uk.ac.ox.poseidon.geography.bathymetry.Factories.uniformBathymetricGrid;

import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.SimulationScope;
import uk.ac.ox.poseidon.geography.Envelope;
import uk.ac.ox.poseidon.geography.grids.Cell;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;

final class FactoriesTest {

  private final ModelGrid modelGrid = ModelGrid.create(
    2,
    2,
    new Envelope(0, 2, 0, 2)
  );

  @Test
  void uniformBathymetricGridPutsEveryCellAtTheGivenDepth() {
    final BathymetricGrid grid = uniformBathymetricGrid(
      ignored -> modelGrid,
      30
    ).get(GLOBAL_SCOPE);

    assertThat(grid.getDepth(new Cell(1, 0))).isEqualTo(30.0);
    assertThat(grid.getWaterCells()).hasSize(4);
  }

  @Test
  void roughCoastalBathymetricGridKeepsEachArgumentInItsPlace() {
    final Factory<SimulationScope, ModelGrid> grid = ignored -> modelGrid;

    final var factory = roughCoastalBathymetricGrid(
      grid,
      1,
      2,
      0.25,
      3,
      -50,
      80,
      0.125
    );

    assertThat(factory.getModelGrid()).isSameAs(grid);
    assertThat(factory.getCoastalRoughness()).isEqualTo(1);
    assertThat(factory.getSmoothingIterations()).isEqualTo(2);
    assertThat(factory.getSmoothingStrength()).isEqualTo(0.25);
    assertThat(factory.getMaximumLandWidth()).isEqualTo(3);
    assertThat(factory.getMinimumElevation()).isEqualTo(-50.0);
    assertThat(factory.getMaximumElevation()).isEqualTo(80.0);
    assertThat(factory.getProbabilityOfFlippingLandToWater()).isEqualTo(0.125);
  }
}
