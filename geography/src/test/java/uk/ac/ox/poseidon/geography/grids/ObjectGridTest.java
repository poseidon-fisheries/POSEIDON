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

import org.junit.jupiter.api.Test;
import sim.field.grid.SparseGrid2D;
import sim.util.Int2D;
import uk.ac.ox.poseidon.geography.Envelope;

final class ObjectGridTest {

  private final ModelGrid modelGrid = ModelGrid.create(
    3,
    2,
    new Envelope(0, 3, 0, 2)
  );

  @Test
  void iteratingVisitsEveryObjectOnTheGrid() {
    final var grid = gridWith("a", new Cell(0, 0), "b", new Cell(2, 1));

    assertThat(grid.iterator())
      .toIterable()
      .containsExactlyInAnyOrder("a", "b");
  }

  @Test
  void anEmptyGridHasNothingToIterate() {
    final var grid = new StringGrid(modelGrid);

    assertThat(grid.iterator().hasNext()).isFalse();
    assertThat(grid.stream()).isEmpty();
  }

  @Test
  void streamingYieldsEveryObjectOnTheGrid() {
    final var grid = gridWith("a", new Cell(0, 0), "b", new Cell(2, 1));

    assertThat(grid.stream()).containsExactlyInAnyOrder("a", "b");
  }

  @Test
  void objectsSharingACellAreAllYielded() {
    final var grid = gridWith("a", new Cell(1, 1), "b", new Cell(1, 1));

    assertThat(grid.stream()).containsExactlyInAnyOrder("a", "b");
    assertThat(grid.numObjectsAt(new Cell(1, 1))).isEqualTo(2);
  }

  @Test
  void theCopyOfTheFieldHoldsTheSameObjectsAtTheSameLocations() {
    final var grid = gridWith("a", new Cell(0, 0), "b", new Cell(2, 1));

    final var copy = grid.copyOfField();

    assertThat(copy).isNotSameAs(grid.field);
    assertThat(copy.getWidth()).isEqualTo(3);
    assertThat(copy.getHeight()).isEqualTo(2);
    assertThat(copy.getObjectLocation("a")).isEqualTo(new Int2D(0, 0));
    assertThat(copy.getObjectLocation("b")).isEqualTo(new Int2D(2, 1));
  }

  @Test
  void addingAnObjectToTheCopyOfTheFieldLeavesTheGridUnchanged() {
    final var grid = gridWith("a", new Cell(0, 0), "b", new Cell(2, 1));

    final var copy = grid.copyOfField();
    copy.setObjectLocation("c", 1, 1);

    assertThat(grid.stream()).containsExactlyInAnyOrder("a", "b");
    assertThat(grid.anyObjectsAt(new Cell(1, 1))).isFalse();
  }

  private StringGrid gridWith(
    final String first,
    final Cell firstCell,
    final String second,
    final Cell secondCell
  ) {
    final var field = new SparseGrid2D(3, 2);
    field.setObjectLocation(first, firstCell.getX(), firstCell.getY());
    field.setObjectLocation(second, secondCell.getX(), secondCell.getY());
    return new StringGrid(modelGrid, field);
  }

  private static final class StringGrid extends ObjectGrid<String> {

    StringGrid(final ModelGrid modelGrid) {
      super(modelGrid);
    }

    StringGrid(final ModelGrid modelGrid, final SparseGrid2D field) {
      super(modelGrid, field);
    }

    @Override
    protected String getObjectId(final String object) {
      return object;
    }
  }
}
