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

import com.google.common.collect.ImmutableSet;
import ec.util.MersenneTwisterFast;
import sim.field.grid.ObjectGrid2D;
import sim.util.Double2D;
import sim.util.Int2D;
import sim.util.Number2D;
import uk.ac.ox.poseidon.geography.Coordinate;
import uk.ac.ox.poseidon.geography.Envelope;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * The rectangular cell grid underlying every other spatial component in this module: maps between
 * geographic {@link Coordinate}s, continuous grid points ({@link Double2D}), and discrete cells
 * ({@link Int2D}), and tracks which cells are "active" (participate in the simulation) versus
 * merely present. Built via {@link #create}/{@link #withActiveCells}, or
 * {@link uk.ac.ox.poseidon.geography.grids.Factories} for the {@code Factory}-facing entry
 * points.
 */
public interface ModelGrid {

    /**
     * @param gridWidth  the number of cells wide
     * @param gridHeight the number of cells tall
     * @param envelope   the geographic bounding box the grid covers
     * @return a new grid, of size {@code gridWidth x gridHeight}, with every cell active
     */
    static ModelGrid create(
        final int gridWidth,
        final int gridHeight,
        final Envelope envelope
    ) {
        return new ModelGridWithAllCellsActive(
            gridWidth,
            gridHeight,
            envelope
        );
    }

    /**
     * @param activeCells the cells that should be active in the returned grid
     * @return a grid with the same dimensions and envelope as this one, but with only
     * {@code activeCells} active (or, if that's every cell, an all-active grid)
     */
    default ModelGrid withActiveCells(final Collection<Int2D> activeCells) {
        final Set<Int2D> activeCellSet = Set.copyOf(activeCells);
        return getAllCells().allMatch(activeCellSet::contains)
            ? create(getGridWidth(), getGridHeight(), getEnvelope())
            : new ModelGridWithSomeCellsActive(
                getGridWidth(),
                getGridHeight(),
                getEnvelope(),
                activeCellSet::contains
            );
    }

    /** @return every cell in the grid, active or not */
    Stream<Int2D> getAllCells();

    /**
     * Dispatches the correct method to convert a Number2D to a coordinate. Note that, in the case
     * of Int2D, e.g. (3, 2), we assume that we are referring to the cell itself and return the
     * coordinate at the centre of the cell (3.5, 2.5) instead of at its corner (3.0, 2.0). There is
     * potential for confusion, so be careful.
     */
    default Coordinate toCoordinate(final Number2D number2D) {
        return switch (number2D) {
            case final Int2D cell -> toCoordinate(cell);
            case final Double2D point -> toCoordinate(point);
            default -> throw new IllegalStateException("Unexpected value: " + number2D);
        };
    }

    /** @return {@code cell}'s coordinate, at the centre of the cell */
    Coordinate toCoordinate(Int2D cell);

    /** @throws IllegalArgumentException if {@code point} is outside the grid */
    default Coordinate toCoordinate(final Double2D point) {
        checkArgument(isInGrid(point), "%s outside of grid", point);
        return new Coordinate(
            getEnvelope().getMinX() + point.x * getCellWidth(),
            getEnvelope().getMinY() + (getGridHeight() - point.y) * getCellHeight()
        );
    }

    /** @return {@code coordinate}'s position as a continuous grid point */
    Double2D toPoint(Coordinate coordinate);

    /** @return the continuous grid point at the centre of {@code cell} */
    @SuppressWarnings("MagicNumber")
    default Double2D toPoint(final Int2D cell) {
        return new Double2D(cell.getX() + 0.5, cell.getY() + 0.5);
    }

    /** @return the discrete cell containing {@code coordinate} */
    default Int2D toCell(final Coordinate coordinate) {
        return toCell(toPoint(coordinate));
    }

    /** @return the discrete cell containing {@code point} */
    default Int2D toCell(final Double2D point) {
        return new Int2D((int) point.x, (int) point.y);
    }

    /** @return {@code cell}'s Moore neighbours, within a neighbourhood size of 1 */
    default List<Int2D> getNeighbours(final Int2D cell) {
        return getNeighbours(cell, 1);
    }

    /** @return {@code cell}'s Moore neighbours, within the given neighbourhood size */
    List<Int2D> getNeighbours(
        Int2D cell,
        @SuppressWarnings("SameParameterValue") int neighbourhoodSize
    );

    /**
     * @return {@code cell}'s Moore neighbours that are also active, within the given
     * neighbourhood size
     */
    List<Int2D> getActiveNeighbours(
        Int2D cell,
        @SuppressWarnings("SameParameterValue") int neighbourhoodSize
    );

    /** @return a uniformly-random cell in the grid, active or not */
    default Int2D randomCell(final MersenneTwisterFast rng) {
        final int x = rng.nextInt(getGridWidth());
        final int y = rng.nextInt(getGridHeight());
        return new Int2D(x, y);
    }

    /**
     * @return a fresh {@code [gridWidth][gridHeight]} array, e.g. for building a
     * {@link DoubleGrid}
     */
    default double[][] makeDoubleArray() {
        return new double[getGridWidth()][getGridHeight()];
    }

    /**
     * @return {@code cell}'s Moore neighbours that are also active, within a neighbourhood size
     * of 1
     */
    default List<Int2D> getActiveNeighbours(
        final Int2D cell
    ) {
        return getActiveNeighbours(cell, 1);
    }

    /** @throws IllegalArgumentException if {@code coordinate} is outside the grid */
    default void checkIsInGrid(final Coordinate coordinate) {
        checkArgument(
            isInGrid(coordinate),
            "Coordinate %s is outside of grid %s.",
            coordinate,
            getEnvelope()
        );
    }

    /** @throws IllegalArgumentException if {@code cell} is outside the grid */
    default void checkIsInGrid(final Int2D cell) {
        checkArgument(
            isInGrid(cell),
            "Cell %s is outside of grid (max x: %s, max y: %s)",
            cell, getGridWidth() - 1, getGridHeight() - 1
        );
    }

    /** @return whether {@code coordinate} falls within the grid's envelope */
    default boolean isInGrid(final Coordinate coordinate) {
        return getEnvelope().intersects(coordinate);
    }

    /** @return whether {@code point}'s cell is within the grid's bounds */
    default boolean isInGrid(final Double2D point) {
        return isInGrid(toCell(point));
    }

    /** @return whether {@code cell} is within the grid's bounds (does not check activeness) */
    default boolean isInGrid(final Int2D cell) {
        return cell.x >= 0 && cell.y >= 0 && cell.x < getGridWidth() && cell.y < getGridHeight();
    }

    /** @return whether {@code coordinate}'s cell is active */
    default boolean isActive(final Coordinate coordinate) {
        return isActive(toCell(coordinate));
    }

    /** @return whether {@code point}'s cell is active */
    default boolean isActive(final Double2D point) {
        return isActive(toCell(point));
    }

    /** @return whether {@code cell} participates in the simulation */
    boolean isActive(Int2D cell);

    /** @return the number of cells wide */
    int getGridWidth();

    /** @return the number of cells tall */
    int getGridHeight();

    /** @return the geographic bounding box the grid covers */
    Envelope getEnvelope();

    /** @return the width of one cell, in degrees of longitude */
    double getCellWidth();

    /** @return the height of one cell, in degrees of latitude */
    double getCellHeight();

    /** @return every active cell in the grid */
    ImmutableSet<Int2D> getActiveCells();

    /** @return a MASON grid holding each cell's {@link Coordinate}, for direct MASON interop */
    ObjectGrid2D getCoordinatesGrid();
}
