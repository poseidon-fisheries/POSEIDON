/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024 CoHESyS Lab cohesys.lab@gmail.com
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
 *
 */

package uk.ac.ox.poseidon.geography.grids;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import ec.util.MersenneTwisterFast;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import sim.field.grid.Grid2D;
import sim.field.grid.ObjectGrid2D;
import sim.field.grid.SparseGrid2D;
import sim.util.Double2D;
import sim.util.Int2D;
import sim.util.IntBag;
import sim.util.Number2D;
import uk.ac.ox.poseidon.geography.Coordinate;
import uk.ac.ox.poseidon.geography.Envelope;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.util.Map.entry;
import static java.util.function.Function.identity;
import static java.util.function.Predicate.not;
import static java.util.stream.IntStream.range;

@Getter
@ToString
@EqualsAndHashCode
public final class ModelGrid {

    private final int gridWidth;   // the width in cells
    private final int gridHeight;  // the height in cells
    private final Envelope envelope;
    private final double cellWidth;   // the width of a cell in degrees
    private final double cellHeight;  // the height of a cell in degrees

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private final Int2D[] allCells;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private final LoadingCache<Entry<Int2D, Integer>, List<Int2D>> mooreNeighbourhoods =
        CacheBuilder.newBuilder().build(CacheLoader.from(this::computeMooreNeighbourhood));
    // Using ImmutableSet here as it should provide fast lookup _and_ iteration
    @ToString.Exclude
    private final ImmutableSet<Int2D> activeCells;
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private final LoadingCache<Entry<Int2D, Integer>, List<Int2D>> activeMooreNeighbourhoods =
        CacheBuilder.newBuilder().build(CacheLoader.from(this::computeActiveMooreNeighbourhood));
    private final ObjectGrid2D coordinatesGrid;

    private ModelGrid(
        final int gridWidth,
        final int gridHeight,
        final Envelope envelope,
        final Int2D[] allCells,
        final ImmutableSet<Int2D> activeCells
    ) {
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
        this.envelope = envelope;
        this.cellWidth = envelope.getWidth() / (double) this.getGridWidth();
        this.cellHeight = envelope.getHeight() / (double) this.getGridHeight();
        this.allCells = allCells;
        this.activeCells = activeCells;
        this.coordinatesGrid = makeCoordinateGrid();
    }

    public static ModelGrid withActiveCells(
        final int gridWidth,
        final int gridHeight,
        final Envelope envelope,
        final Collection<Int2D> activeCells
    ) {
        final Set<Int2D> activeCellsSet = toSet(activeCells);
        return makeModelGrid(gridWidth, gridHeight, envelope, activeCellsSet::contains);
    }

    private static Set<Int2D> toSet(final Collection<Int2D> cells) {
        return cells instanceof Set<Int2D>
            ? (Set<Int2D>) cells
            : ImmutableSet.copyOf(cells);
    }

    public static ModelGrid withInactiveCells(
        final int gridWidth,
        final int gridHeight,
        final Envelope envelope,
        final Collection<Int2D> inactiveCells
    ) {
        final Set<Int2D> inactiveCellSet = toSet(inactiveCells);
        return makeModelGrid(gridWidth, gridHeight, envelope, not(inactiveCellSet::contains));
    }

    private static ModelGrid makeModelGrid(
        final int gridWidth,
        final int gridHeight,
        final Envelope envelope,
        final Predicate<Int2D> activePredicate
    ) {
        final Int2D[] allCells = makeAllCellsArray(gridWidth, gridHeight);
        return new ModelGrid(
            gridWidth,
            gridHeight,
            envelope,
            allCells,
            Arrays.stream(allCells).filter(activePredicate).collect(toImmutableSet())
        );
    }

    public static ModelGrid withAllCellsActive(
        final int gridWidth,
        final int gridHeight,
        final Envelope envelope
    ) {
        return makeModelGrid(gridWidth, gridHeight, envelope, __ -> true);
    }

    private static Int2D[] makeAllCellsArray(
        final int gridWidth,
        final int gridHeight
    ) {
        checkArgument(gridWidth > 0);
        checkArgument(gridHeight > 0);
        return range(0, gridWidth)
            .mapToObj(x ->
                range(0, gridHeight)
                    .mapToObj(y -> new Int2D(x, y))
            )
            .flatMap(identity())
            .toArray(Int2D[]::new);
    }

    private ObjectGrid2D makeCoordinateGrid() {
        final ObjectGrid2D objectGrid2D = new ObjectGrid2D(gridWidth, gridHeight);
        for (int x = 0; x < gridWidth; x++) {
            for (int y = 0; y < gridHeight; y++) {
                objectGrid2D.set(x, y, toCoordinate(toPoint(new Int2D(x, y))));
            }
        }
        return objectGrid2D;
    }

    public Stream<Int2D> getAllCells() {
        return Arrays.stream(allCells);
    }

    /**
     * Dispatches the correct method to convert a Number2D to a coordinate. Note that, in the case
     * of Int2D, e.g. (3, 2), we assume that we are referring to the cell itself and return the
     * coordinate at the centre of the cell (3.5, 2.5) instead of at its corner (3.0, 2.0). There is
     * potential for confusion, so be careful.
     */
    public Coordinate toCoordinate(final Number2D number2D) {
        return switch (number2D) {
            case final Int2D cell -> toCoordinate(cell);
            case final Double2D point -> toCoordinate(point);
            default -> throw new IllegalStateException("Unexpected value: " + number2D);
        };
    }

    public Coordinate toCoordinate(final Int2D cell) {
        checkArgument(isInGrid(cell), "%s outside of grid", cell);
        return (Coordinate) coordinatesGrid.get(cell.x, cell.y);
    }

    public Coordinate toCoordinate(final Double2D point) {
        checkArgument(isInGrid(point), "%s outside of grid", point);
        return new Coordinate(
            envelope.getMinX() + point.x * cellWidth,
            envelope.getMinY() + (gridHeight - point.y) * cellHeight
        );
    }

    /**
     * Transforms a lon/lat coordinate to an x/y coordinate that can be used with a continuous field
     * covering the same space as the nautical map. This is basically a floating point version of
     * vectors.size().toXCoord/.toYCoord; not sure why it doesn't exist in GeomVectorField in the
     * first place...
     */
    public Double2D toPoint(final Coordinate coordinate) {
        final double pixelWidth = envelope.getWidth() / gridWidth;
        final double pixelHeight = envelope.getHeight() / gridHeight;
        final double x = (coordinate.lon - envelope.getMinX()) / pixelWidth;
        final double y = (envelope.getMaxY() - coordinate.lat) / pixelHeight;
        return new Double2D(x, y);
    }

    @SuppressWarnings("MagicNumber")
    public Double2D toPoint(final Int2D cell) {
        return new Double2D(cell.getX() + 0.5, cell.getY() + 0.5);
    }

    public Int2D toCell(final Coordinate coordinate) {
        return toCell(toPoint(coordinate));
    }

    public Int2D toCell(final Double2D point) {
        return new Int2D((int) point.x, (int) point.y);
    }

    public List<Int2D> getNeighbours(
        final Int2D cell,
        @SuppressWarnings("SameParameterValue") final int neighbourhoodSize
    ) {
        return mooreNeighbourhoods.getUnchecked(entry(cell, neighbourhoodSize));
    }

    public List<Int2D> getActiveNeighbours(
        final Int2D cell,
        @SuppressWarnings("SameParameterValue") final int neighbourhoodSize
    ) {
        return activeMooreNeighbourhoods.getUnchecked(entry(cell, neighbourhoodSize));
    }

    private List<Int2D> computeActiveMooreNeighbourhood(
        final Entry<Int2D, Integer> entry
    ) {
        return mooreNeighbourhoods
            .getUnchecked(entry)
            .stream()
            .filter(this::isActive)
            .toList();
    }

    @SuppressWarnings("UnstableApiUsage")
    private List<Int2D> computeMooreNeighbourhood(
        final Entry<Int2D, Integer> entry
    ) {
        final Int2D cell = entry.getKey();
        final int neighbourhoodSize = entry.getValue();
        final IntBag xPositions = new IntBag(8);
        final IntBag yPositions = new IntBag(8);
        // `Grid2D::getMooreLocations` fills the provided bags as a side effect
        new SparseGrid2D(gridWidth, gridHeight)
            .getMooreLocations(
                cell.x,
                cell.y,
                neighbourhoodSize,
                Grid2D.BOUNDED,
                false,
                xPositions,
                yPositions
            );
        return Streams
            .zip(
                Arrays.stream(xPositions.toArray()).boxed(),
                Arrays.stream(yPositions.toArray()).boxed(),
                Int2D::new
            )
            .toList();
    }

    public Int2D randomCell(final MersenneTwisterFast rng) {
        final int x = rng.nextInt(getGridWidth());
        final int y = rng.nextInt(getGridHeight());
        return new Int2D(x, y);
    }

    public double[][] makeDoubleArray() {
        return new double[getGridWidth()][getGridHeight()];
    }

    public List<Int2D> getActiveNeighbours(
        final Int2D cell
    ) {
        return getActiveNeighbours(cell, 1);
    }

    public boolean isInGrid(final Coordinate coordinate) {
        return isInGrid(toCell(coordinate));
    }

    public boolean isInGrid(final Double2D point) {
        return isInGrid(toCell(point));
    }

    public boolean isInGrid(final Int2D cell) {
        return cell.x >= 0 && cell.y >= 0 && cell.x < gridWidth && cell.y < gridHeight;
    }

    public boolean isActive(final Coordinate coordinate) {
        return isActive(toCell(coordinate));
    }

    public boolean isActive(final Double2D point) {
        return isActive(toCell(point));
    }

    public boolean isActive(final Int2D cell) {
        return activeCells.contains(cell);
    }

}

