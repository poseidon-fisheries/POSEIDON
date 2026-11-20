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

package uk.ac.ox.poseidon.geography.grids;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import sim.field.grid.DoubleGrid2D;
import uk.ac.ox.poseidon.geography.Envelope;

final class AbstractGridTest {

  private final ModelGrid modelGrid = ModelGrid.create(
    3,
    2,
    new Envelope(0, 3, 0, 2)
  );

  @Test
  void aFieldOfAnotherWidthIsRefused() {
    assertThatThrownBy(() ->
      new MutableDoubleGrid(modelGrid, new DoubleGrid2D(2, 2))
    ).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void aFieldOfAnotherHeightIsRefused() {
    assertThatThrownBy(() ->
      new MutableDoubleGrid(modelGrid, new DoubleGrid2D(3, 3))
    ).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void aFieldWithoutColumnsIsRefusedEvenWhenTheModelGridAgrees() {
    final ModelGrid empty = mock(ModelGrid.class);
    when(empty.getGridWidth()).thenReturn(0);
    when(empty.getGridHeight()).thenReturn(2);

    assertThatThrownBy(() ->
      new MutableDoubleGrid(empty, new DoubleGrid2D(0, 2))
    ).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void aFieldWithoutRowsIsRefusedEvenWhenTheModelGridAgrees() {
    final ModelGrid empty = mock(ModelGrid.class);
    when(empty.getGridWidth()).thenReturn(3);
    when(empty.getGridHeight()).thenReturn(0);

    assertThatThrownBy(() ->
      new MutableDoubleGrid(empty, new DoubleGrid2D(3, 0))
    ).isInstanceOf(IllegalArgumentException.class);
  }
}
