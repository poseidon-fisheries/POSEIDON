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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static java.util.stream.Collectors.joining;

/** Writes small ESRI ASCII grid ({@code .asc}) files for the grid-reader tests. */
public final class AscFiles {

    private static final double WEST = 10;
    private static final double SOUTH = 40;
    private static final double CELL_SIZE = 1;

    /**
     * A 3 by 2 grid of one-degree cells from 10°E to 13°E and 40°N to 42°N, with a different
     * value in every cell, so that a flipped or shifted reading shows:
     * <pre>
     *          10°E  11°E  12°E  13°E
     *   42°N  +-----+-----+-----+
     *         |  1  |  2  |  3  |   (north row: image row 0)
     *   41°N  +-----+-----+-----+
     *         |  4  |  5  |  6  |   (south row: image row 1)
     *   40°N  +-----+-----+-----+
     * </pre>
     *
     * @return the fixture's values, a fresh array on every call
     */
    public static double[][] rowsFromNorth() {
        return new double[][]{
            {1, 2, 3},
            {4, 5, 6}
        };
    }

    private AscFiles() {
    }

    /** @return the path of {@link #rowsFromNorth()} written as {@code fileName} in {@code folder} */
    public static Path writeFixture(final Path folder, final String fileName) {
        return write(folder.resolve(fileName), WEST, SOUTH, CELL_SIZE, rowsFromNorth());
    }

    /**
     * Writes an {@code .asc} file, whose data rows go from north to south.
     *
     * @return {@code file}
     */
    public static Path write(
        final Path file,
        final double xllCorner,
        final double yllCorner,
        final double cellSize,
        final double[][] rowsFromNorth
    ) {
        final String header = String.join(
            "\n",
            "ncols " + rowsFromNorth[0].length,
            "nrows " + rowsFromNorth.length,
            "xllcorner " + xllCorner,
            "yllcorner " + yllCorner,
            "cellsize " + cellSize,
            "NODATA_value -9999"
        );
        final String rows = Arrays
            .stream(rowsFromNorth)
            .map(row -> Arrays.stream(row).mapToObj(Double::toString).collect(joining(" ")))
            .collect(joining("\n"));
        try {
            return Files.writeString(file, header + "\n" + rows + "\n");
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
