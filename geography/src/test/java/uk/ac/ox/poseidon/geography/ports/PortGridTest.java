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

import java.util.List;
import org.junit.jupiter.api.Test;
import sim.field.grid.SparseGrid2D;
import uk.ac.ox.poseidon.geography.Envelope;
import uk.ac.ox.poseidon.geography.bathymetry.DefaultBathymetricGrid;
import uk.ac.ox.poseidon.geography.grids.Cell;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;

final class PortGridTest {

  @Test
  void portsCanBeCreatedOnInactiveLandBesideInactiveWater() {
    final var modelGrid = ModelGrid.create(
      2,
      1,
      new Envelope(0, 2, 0, 1)
    ).withActiveCells(List.of());
    final var bathymetricGrid = new DefaultBathymetricGrid(
      modelGrid,
      new double[][] { { 0 }, { -2 } }
    );
    final var portGrid = new MutablePortGrid(
      bathymetricGrid,
      new SparseGrid2D(2, 1)
    );

    final var port = portGrid.createPort("P", "Port P", new Cell(0, 0));

    assertThat(portGrid.getCell(port)).isEqualTo(new Cell(0, 0));
    assertThat(portGrid.getPorts()).containsExactly(port);
  }

  @Test
  void landWithoutWaterNeighboursIsRejected() {
    final var bathymetricGrid = new DefaultBathymetricGrid(new double[][] {
      { 0 },
      { 1 }
    });
    final var portGrid = new MutablePortGrid(
      bathymetricGrid,
      new SparseGrid2D(2, 1)
    );

    assertThatThrownBy(() -> portGrid.validateCell(new Cell(0, 0)))
      .isExactlyInstanceOf(IllegalStateException.class)
      .hasMessageContaining("has no water neighbour");
  }

  @Test
  void waterCellsAreRejectedEvenWhenTheyHaveWaterNeighbours() {
    final var bathymetricGrid = new DefaultBathymetricGrid(new double[][] {
      { -1 },
      { -2 }
    });
    final var portGrid = new MutablePortGrid(
      bathymetricGrid,
      new SparseGrid2D(2, 1)
    );

    assertThatThrownBy(() -> portGrid.validateCell(new Cell(0, 0)))
      .isExactlyInstanceOf(IllegalStateException.class)
      .hasMessageContaining("is not on land");
  }
}
