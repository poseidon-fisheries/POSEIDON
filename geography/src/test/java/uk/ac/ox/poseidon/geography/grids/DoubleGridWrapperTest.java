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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

final class DoubleGridWrapperTest {

  private final ModelGrid modelGrid = mock(ModelGrid.class);
  private final DoubleGrid wrapped = mock(DoubleGrid.class);
  private final DoubleGridWrapper wrapper = new DoubleGridWrapper(wrapped);

  @Test
  void delegatesTheModelGrid() {
    when(wrapped.getModelGrid()).thenReturn(modelGrid);

    assertThat(wrapper.getModelGrid()).isSameAs(modelGrid);
  }

  @Test
  void delegatesTheValueOfACell() {
    final Cell cell = new Cell(1, 2);
    when(wrapped.getValue(cell)).thenReturn(3.5);

    assertThat(wrapper.getValue(cell)).isEqualTo(3.5);
  }

  @Test
  void delegatesTheMinimumValue() {
    when(wrapped.getMinimumValue()).thenReturn(-1.5);

    assertThat(wrapper.getMinimumValue()).isEqualTo(-1.5);
  }

  @Test
  void delegatesTheMaximumValue() {
    when(wrapped.getMaximumValue()).thenReturn(7.25);

    assertThat(wrapper.getMaximumValue()).isEqualTo(7.25);
  }

  @Test
  void delegatesTheSum() {
    when(wrapped.getSum()).thenReturn(42.0);

    assertThat(wrapper.getSum()).isEqualTo(42.0);
  }
}
