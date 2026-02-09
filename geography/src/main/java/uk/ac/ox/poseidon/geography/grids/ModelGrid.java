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

public interface ModelGrid {

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

    Coordinate toCoordinate(Int2D cell);

    default Coordinate toCoordinate(final Double2D point) {
        checkArgument(isInGrid(point), "%s outside of grid", point);
        return new Coordinate(
            getEnvelope().getMinX() + point.x * getCellWidth(),
            getEnvelope().getMinY() + (getGridHeight() - point.y) * getCellHeight()
        );
    }

    Double2D toPoint(Coordinate coordinate);

    @SuppressWarnings("MagicNumber")
    default Double2D toPoint(final Int2D cell) {
        return new Double2D(cell.getX() + 0.5, cell.getY() + 0.5);
    }

    default Int2D toCell(final Coordinate coordinate) {
        return toCell(toPoint(coordinate));
    }

    default Int2D toCell(final Double2D point) {
        return new Int2D((int) point.x, (int) point.y);
    }

    default List<Int2D> getNeighbours(final Int2D cell) {
        return getNeighbours(cell, 1);
    }

    List<Int2D> getNeighbours(
        Int2D cell,
        @SuppressWarnings("SameParameterValue") int neighbourhoodSize
    );

    List<Int2D> getActiveNeighbours(
        Int2D cell,
        @SuppressWarnings("SameParameterValue") int neighbourhoodSize
    );

    default Int2D randomCell(final MersenneTwisterFast rng) {
        final int x = rng.nextInt(getGridWidth());
        final int y = rng.nextInt(getGridHeight());
        return new Int2D(x, y);
    }

    default double[][] makeDoubleArray() {
        return new double[getGridWidth()][getGridHeight()];
    }

    default List<Int2D> getActiveNeighbours(
        final Int2D cell
    ) {
        return getActiveNeighbours(cell, 1);
    }

    default void checkIsInGrid(final Coordinate coordinate) {
        checkArgument(
            isInGrid(coordinate),
            "Coordinate %s is outside of grid %s.",
            coordinate,
            getEnvelope()
        );
    }

    default void checkIsInGrid(final Int2D cell) {
        checkArgument(
            isInGrid(cell),
            "Cell %s is outside of grid (max x: %s, max y: %s)",
            cell, getGridWidth() - 1, getGridHeight() - 1
        );
    }

    default boolean isInGrid(final Coordinate coordinate) {
        return getEnvelope().intersects(coordinate);
    }

    default boolean isInGrid(final Double2D point) {
        return isInGrid(toCell(point));
    }

    default boolean isInGrid(final Int2D cell) {
        return cell.x >= 0 && cell.y >= 0 && cell.x < getGridWidth() && cell.y < getGridHeight();
    }

    default boolean isActive(final Coordinate coordinate) {
        return isActive(toCell(coordinate));
    }

    default boolean isActive(final Double2D point) {
        return isActive(toCell(point));
    }

    boolean isActive(Int2D cell);

    int getGridWidth();

    int getGridHeight();

    Envelope getEnvelope();

    double getCellWidth();

    double getCellHeight();

    ImmutableSet<Int2D> getActiveCells();

    ObjectGrid2D getCoordinatesGrid();
}
