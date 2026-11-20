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

package uk.ac.ox.poseidon.geography.ports;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ec.util.MersenneTwisterFast;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.ProductStore;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.SimulationScope;
import uk.ac.ox.poseidon.geography.Envelope;
import uk.ac.ox.poseidon.geography.bathymetry.BathymetricGrid;
import uk.ac.ox.poseidon.geography.bathymetry.DefaultBathymetricGrid;
import uk.ac.ox.poseidon.geography.grids.Cell;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;

final class RandomCellsPortGridFactoryTest {

  private final Simulation simulation = mock(Simulation.class);
  private final SimulationScope scope = new SimulationScope(simulation);
  private final MersenneTwisterFast rng = mock(MersenneTwisterFast.class);

  @BeforeEach
  void setUp() {
    when(simulation.getProductStore()).thenReturn(new ProductStore());
  }

  @Test
  void exactlyEnoughSuitableLandCellsCanAllReceivePorts() {
    final var bathymetricGrid = alternatingLandAndWater();

    final var portGrid = factory(bathymetricGrid, 3, 1).get(scope);

    assertThat(
      portGrid.getPorts().map(portGrid::getCell)
    ).containsExactlyInAnyOrder(new Cell(0, 0), new Cell(2, 0), new Cell(4, 0));
    assertThat(portGrid.getBathymetricGrid()).isSameAs(bathymetricGrid);
    assertThat(
      portGrid.getPorts().map(Port::getCode)
    ).containsExactlyInAnyOrder("P0_0", "P2_0", "P4_0");
    assertThat(
      portGrid.getPorts().map(Port::getName)
    ).containsExactlyInAnyOrder("Port P0_0", "Port P2_0", "Port P4_0");
  }

  @Test
  void theWaterNeighbourMinimumIncludesEqualityAndExcludesLowerCounts() {
    final var portGrid = factory(alternatingLandAndWater(), 1, 2).get(scope);

    assertThat(portGrid.getPorts().map(portGrid::getCell)).containsExactly(
      new Cell(2, 0)
    );
  }

  @Test
  void diagonallyAdjacentWaterCountsTowardsTheMinimum() {
    final BathymetricGrid bathymetricGrid = new DefaultBathymetricGrid(
      new double[][] { { -1, -1, -1 }, { -1, 0, -1 }, { -1, -1, -1 } }
    );

    final var portGrid = factory(bathymetricGrid, 1, 8).get(scope);

    assertThat(portGrid.getPorts().map(portGrid::getCell)).containsExactly(
      new Cell(1, 1)
    );
  }

  @Test
  void inactiveWaterCountsTowardsTheMinimum() {
    final var modelGrid = ModelGrid.create(
      3,
      1,
      new Envelope(0, 3, 0, 1)
    ).withActiveCells(List.of(new Cell(1, 0), new Cell(2, 0)));
    final BathymetricGrid bathymetricGrid = new DefaultBathymetricGrid(
      modelGrid,
      new double[][] { { -1 }, { 0 }, { -1 } }
    );

    final var portGrid = factory(bathymetricGrid, 1, 2).get(scope);

    assertThat(portGrid.getPorts().map(portGrid::getCell)).containsExactly(
      new Cell(1, 0)
    );
  }

  @Test
  void tooFewSuitableCellsFailsBeforePlacingAnyPorts() {
    assertThatThrownBy(() ->
      factory(alternatingLandAndWater(), 4, 1).get(scope)
    )
      .isExactlyInstanceOf(IllegalStateException.class)
      .hasMessage("Only 3 suitable land tiles for 4 ports.");
  }

  @Test
  void noLandCellsFailsWhenAPortIsRequested() {
    final BathymetricGrid bathymetricGrid = new DefaultBathymetricGrid(
      new double[][] { { -1 }, { -1 } }
    );

    assertThatThrownBy(() -> factory(bathymetricGrid, 1, 1).get(scope))
      .isExactlyInstanceOf(IllegalStateException.class)
      .hasMessage("Only 0 suitable land tiles for 1 ports.");
  }

  @Test
  void zeroRequestedPortsAllowsAnEmptyCandidateList() {
    final BathymetricGrid bathymetricGrid = new DefaultBathymetricGrid(
      new double[][] { { -1 }, { -1 } }
    );

    assertThat(factory(bathymetricGrid, 0, 1).get(scope).getPorts()).isEmpty();
  }

  @Test
  void theSuppliedRandomStreamSelectsDistinctCellsUpToTheRequestedCount() {
    when(rng.nextInt(3)).thenReturn(2);
    when(rng.nextInt(2)).thenReturn(0);

    final var portGrid = factory(alternatingLandAndWater(), 2, 1).get(scope);

    assertThat(
      portGrid.getPorts().map(portGrid::getCell)
    ).containsExactlyInAnyOrder(new Cell(4, 0), new Cell(2, 0));
    assertThat(
      portGrid.getPorts().map(Port::getCode)
    ).containsExactlyInAnyOrder("P4_0", "P2_0");
  }

  private RandomCellsPortGridFactory factory(
    final BathymetricGrid bathymetricGrid,
    final int numberOfPorts,
    final int minimumAdjacentWaterTiles
  ) {
    return new RandomCellsPortGridFactory(
      ignored -> bathymetricGrid,
      ignored -> (context, cell) -> "P" + cell.getX() + "_" + cell.getY(),
      numberOfPorts,
      minimumAdjacentWaterTiles,
      ignored -> rng
    );
  }

  private static BathymetricGrid alternatingLandAndWater() {
    return new DefaultBathymetricGrid(new double[][] {
      { 0 },
      { -1 },
      { 0 },
      { -1 },
      { 0 }
    });
  }
}
