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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import sim.util.Int2D;
import uk.ac.ox.poseidon.core.scopes.Scope;
import uk.ac.ox.poseidon.geography.Coordinate;

import java.nio.file.Path;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static uk.ac.ox.poseidon.geography.grids.AscFiles.writeFixture;
import static uk.ac.ox.poseidon.geography.grids.Factories.cellSetFromGridFile;
import static uk.ac.ox.poseidon.geography.grids.Factories.modelGridFromGridFile;

class CellSetFromGridFileFactoryTest {

    @TempDir
    private Path tempDir;

    private Set<Int2D> cellsWith(final Path file, final double includedValue) {
        return cellSetFromGridFile(scope -> file, includedValue).get(mock(Scope.class));
    }

    @Test
    void cellsAreTheFilesColumnsAndRowsFromTheNorth() {
        final Path file = writeFixture(tempDir, "fixture.asc");
        assertThat(cellsWith(file, 2)).containsExactly(new Int2D(1, 0));
        assertThat(cellsWith(file, 4)).containsExactly(new Int2D(0, 1));
    }

    @Test
    void everyCellWithTheValueIsIncluded() {
        final Path file = AscFiles.write(
            tempDir.resolve("two_values.asc"), 10, 40, 1,
            new double[][]{{0, 1, 0}, {1, 0, 1}}
        );
        assertThat(cellsWith(file, 0))
            .containsExactlyInAnyOrder(new Int2D(0, 0), new Int2D(2, 0), new Int2D(1, 1));
    }

    @Test
    void noCellIsIncludedWhenNoneHasTheValue() {
        assertThat(cellsWith(writeFixture(tempDir, "fixture.asc"), 7)).isEmpty();
    }

    @Test
    void cellsMatchTheModelGridBuiltFromAFileWithTheSameHeader() {
        // How NW Med builds its active cells: the model grid from one file, the cell set from
        // another with the same header. A cell from the set must be where its value is.
        final Path file = writeFixture(tempDir, "fixture.asc");
        final ModelGrid modelGrid = modelGridFromGridFile(scope -> file).get(mock(Scope.class));
        final Int2D cellWithTwo = cellsWith(file, 2).iterator().next();
        assertThat(modelGrid.toCoordinate(cellWithTwo)).isEqualTo(new Coordinate(11.5, 41.5));
    }

    @Test
    void aPathThatIsNotAFileFails() {
        final Path missing = tempDir.resolve("missing.asc");
        assertThatThrownBy(() -> cellsWith(missing, 0))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("does not point to a file");
    }
}
