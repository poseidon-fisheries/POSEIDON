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
import static tech.units.indriya.unit.Units.GRAM;
import static uk.ac.ox.poseidon.biology.biomass.Factories.totalCarryingCapacityGrid;
import static uk.ac.ox.poseidon.biology.biomass.Factories.uniformCarryingCapacityGrid;

import java.util.List;
import org.junit.jupiter.api.Test;
import tech.units.indriya.quantity.Quantities;
import uk.ac.ox.poseidon.core.Scope;
import uk.ac.ox.poseidon.geography.Envelope;
import uk.ac.ox.poseidon.geography.bathymetry.BathymetricGrid;
import uk.ac.ox.poseidon.geography.bathymetry.DefaultBathymetricGrid;
import uk.ac.ox.poseidon.geography.grids.Cell;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;

final class CarryingCapacityFactoriesTest {

  private final ModelGrid modelGrid = ModelGrid.create(
    4,
    1,
    new Envelope(0, 4, 0, 1)
  ).withActiveCells(List.of(new Cell(0, 0), new Cell(1, 0), new Cell(2, 0)));
  private final BathymetricGrid bathymetricGrid = new DefaultBathymetricGrid(
    modelGrid,
    new double[][] { { -10 }, { -20 }, { 0 }, { -30 } }
  );

  @Test
  void uniformCapacityConvertsToKilogramsAndAppliesOnlyToActiveWater() {
    final CarryingCapacityGrid grid = uniformCarryingCapacityGrid(
      scope -> modelGrid,
      scope -> bathymetricGrid,
      scope -> Quantities.getQuantity(6000, GRAM)
    ).get(Scope.GLOBAL_SCOPE);

    assertThat(grid.getCarryingCapacity(new Cell(0, 0))).isEqualTo(6);
    assertThat(grid.getCarryingCapacity(new Cell(1, 0))).isEqualTo(6);
    assertThat(grid.getCarryingCapacity(new Cell(2, 0))).isNaN();
    assertThat(grid.getCarryingCapacity(new Cell(3, 0))).isNaN();
    assertThat(grid.getHabitableCells()).containsExactlyInAnyOrder(
      new Cell(0, 0),
      new Cell(1, 0)
    );
  }

  @Test
  void totalCapacityConvertsToKilogramsAndSplitsOnlyAcrossActiveWater() {
    final CarryingCapacityGrid grid = totalCarryingCapacityGrid(
      scope -> modelGrid,
      scope -> bathymetricGrid,
      scope -> Quantities.getQuantity(6000, GRAM)
    ).get(Scope.GLOBAL_SCOPE);

    assertThat(grid.getCarryingCapacity(new Cell(0, 0))).isEqualTo(3);
    assertThat(grid.getCarryingCapacity(new Cell(1, 0))).isEqualTo(3);
    assertThat(grid.getCarryingCapacity(new Cell(2, 0))).isNaN();
    assertThat(grid.getCarryingCapacity(new Cell(3, 0))).isNaN();
    assertThat(grid.getHabitableCells()).containsExactlyInAnyOrder(
      new Cell(0, 0),
      new Cell(1, 0)
    );
  }
}
