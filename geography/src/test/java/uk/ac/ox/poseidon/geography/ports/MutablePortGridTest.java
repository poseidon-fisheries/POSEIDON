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

import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.geography.Coordinate;
import uk.ac.ox.poseidon.geography.bathymetry.DefaultBathymetricGrid;

class MutablePortGridTest {

  @Test
  void copyOfKeepsCells() {
    final DefaultBathymetricGrid bathymetricGrid = new DefaultBathymetricGrid(
      new double[][] { { +1d, -1d, -1d }, { +1d, +1d, -1d }, { -1d, -1d, -1d } }
    );
    final Port portA = new Port("PA", "Port A");
    final Port portB = new Port("PB", "Port B");
    final MutablePortGrid source = new MutablePortGrid(
      bathymetricGrid,
      Map.of(portA, new Coordinate(0.5, 2.5), portB, new Coordinate(1.5, 1.5))
    );

    final MutablePortGrid copy = MutablePortGrid.copyOf(source);

    assertThat(copy).isNotSameAs(source);
    assertThat(copy.getPorts()).containsExactlyInAnyOrderElementsOf(
      source.getPorts().toList()
    );
    Set.of(portA, portB).forEach(port ->
      assertThat(copy.getCell(port)).isEqualTo(source.getCell(port))
    );
  }

  @Test
  void findsAPortByItsCode() {
    final DefaultBathymetricGrid bathymetricGrid = new DefaultBathymetricGrid(
      new double[][] { { +1d, -1d, -1d }, { +1d, +1d, -1d }, { -1d, -1d, -1d } }
    );
    final Port portA = new Port("PA", "Port A");
    final Port portB = new Port("PB", "Port B");
    final MutablePortGrid portGrid = new MutablePortGrid(
      bathymetricGrid,
      Map.of(portA, new Coordinate(0.5, 2.5), portB, new Coordinate(1.5, 1.5))
    );

    assertThat(portGrid.getObject("PB")).contains(portB);
    assertThat(portGrid.getObject("PC")).isEmpty();
  }
}
