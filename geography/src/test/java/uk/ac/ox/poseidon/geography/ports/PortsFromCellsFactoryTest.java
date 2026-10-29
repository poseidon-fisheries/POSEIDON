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
import static uk.ac.ox.poseidon.core.Scope.GLOBAL_SCOPE;
import static uk.ac.ox.poseidon.core.providers.constant.Factories.always;
import static uk.ac.ox.poseidon.core.utils.Factories.object;
import static uk.ac.ox.poseidon.geography.grids.Factories.modelGrid;
import static uk.ac.ox.poseidon.geography.ports.Factories.portsFromCells;

import java.util.List;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.MappingFactory;
import uk.ac.ox.poseidon.core.Scope;
import uk.ac.ox.poseidon.core.utils.Pair;
import uk.ac.ox.poseidon.geography.Coordinate;
import uk.ac.ox.poseidon.geography.grids.Cell;
import uk.ac.ox.poseidon.geography.grids.ModelGridFactory;

class PortsFromCellsFactoryTest {

  private final ModelGridFactory modelGrid = modelGrid(1, 0, 3, 0, 3);

  private final MappingFactory<Scope, Object, Cell, String> cellCode = scope ->
    (context, cell) ->
      cell.x() + "_" + cell.y();

  @Test
  void givesOnePortPerCellInOrderAtTheCellsCentre() {
    final List<Pair<Port, Coordinate>> ports = portsFromCells(
      object(List.of(new Cell(2, 1), new Cell(0, 0))),
      modelGrid,
      cellCode
    ).get(GLOBAL_SCOPE);

    assertThat(ports).containsExactly(
      Pair.of(new Port("2_1", "Port 2_1"), new Coordinate(2.5, 1.5)),
      Pair.of(new Port("0_0", "Port 0_0"), new Coordinate(0.5, 2.5))
    );
  }

  @Test
  void aNullCodeIsAnError() {
    final MappingFactory<Scope, Object, Cell, String> noCode = scope ->
      (context, cell) ->
        null;
    final PortsFromCellsFactory<Scope> ports = portsFromCells(
      object(List.of(new Cell(2, 1))),
      modelGrid,
      noCode
    );

    assertThatThrownBy(() -> ports.get(GLOBAL_SCOPE))
      .isInstanceOf(NullPointerException.class)
      .hasMessage("No port code for Cell[x=2, y=1].");
  }

  @Test
  void twoCellsWithTheSameCodeAreAnError() {
    final PortsFromCellsFactory<Scope> ports = portsFromCells(
      object(List.of(new Cell(2, 1), new Cell(0, 0))),
      modelGrid,
      always("P")
    );

    assertThatThrownBy(() -> ports.get(GLOBAL_SCOPE))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("P=Cell[x=2, y=1]")
      .hasMessageContaining("P=Cell[x=0, y=0]");
  }
}
