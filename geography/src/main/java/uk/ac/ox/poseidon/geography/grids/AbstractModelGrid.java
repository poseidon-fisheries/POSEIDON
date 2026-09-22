/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024-2025, University of Oxford.
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

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.collect.Streams;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import sim.field.grid.Grid2D;
import sim.field.grid.ObjectGrid2D;
import sim.field.grid.SparseGrid2D;
import sim.util.Double2D;
import sim.util.Int2D;
import sim.util.IntBag;
import uk.ac.ox.poseidon.geography.Coordinate;
import uk.ac.ox.poseidon.geography.Envelope;

import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Map.entry;
import static java.util.function.Function.identity;
import static java.util.stream.IntStream.range;

/**
 * A {@link ModelGrid} base class providing the actual coordinate/point/cell math and neighbour
 * computation, caching Moore neighbourhoods (plain and active-filtered) since they're recomputed
 * often. Subclasses only need to supply {@link #isActive}. Instantiated indirectly via
 * {@link ModelGrid#create}/{@link ModelGrid#withActiveCells}.
 */
@Getter
@ToString
@EqualsAndHashCode
abstract class AbstractModelGrid implements ModelGrid {

    private final int gridWidth;   // the width in cells
    private final int gridHeight;  // the height in cells
    private final Envelope envelope;
    private final double cellWidth;   // the width of a cell in degrees
    private final double cellHeight;  // the height of a cell in degrees

    /** Every cell in the grid, active or not, in the order returned by {@link #getAllCells()}. */
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    protected final Int2D[] allCells;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Getter(AccessLevel.PRIVATE)
    private final LoadingCache<Entry<Int2D, Integer>, List<Int2D>> mooreNeighbourhoods =
        Caffeine.newBuilder().build(this::computeMooreNeighbourhood);
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Getter(AccessLevel.PRIVATE)
    private final LoadingCache<Entry<Int2D, Integer>, List<Int2D>> activeMooreNeighbourhoods =
        Caffeine.newBuilder().build(this::computeActiveMooreNeighbourhood);
    private final ObjectGrid2D coordinatesGrid;

    /**
     * @param gridWidth  the number of cells wide
     * @param gridHeight the number of cells tall
     * @param envelope   the geographic bounding box the grid covers
     * @param allCells   every cell in the grid, in the order {@link #getAllCells()} should return
     *                   them
     */
    protected AbstractModelGrid(
        final int gridWidth,
        final int gridHeight,
        final Envelope envelope,
        final Int2D[] allCells
    ) {
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
        this.envelope = envelope;
        this.cellWidth = envelope.getWidth() / (double) this.getGridWidth();
        this.cellHeight = envelope.getHeight() / (double) this.getGridHeight();
        this.allCells = allCells;
        this.coordinatesGrid = makeCoordinateGrid();
    }

    /**
     * @return every cell of a {@code gridWidth x gridHeight} grid, in x-then-y order
     * @throws IllegalArgumentException if {@code gridWidth} or {@code gridHeight} isn't positive
     */
    protected static Int2D[] makeAllCellsArray(
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

    @Override
    public Stream<Int2D> getAllCells() {
        return Arrays.stream(allCells);
    }

    @Override
    public Coordinate toCoordinate(final Int2D cell) {
        checkArgument(isInGrid(cell), "%s outside of grid", cell);
        return (Coordinate) coordinatesGrid.get(cell.x, cell.y);
    }

    /**
     * Transforms a lon/lat coordinate to an x/y coordinate that can be used with a continuous field
     * covering the same space as the nautical map. This is basically a floating point version of
     * vectors.size().toXCoord/.toYCoord; not sure why it doesn't exist in GeomVectorField in the
     * first place...
     */
    @Override
    public Double2D toPoint(final Coordinate coordinate) {
        final double pixelWidth = getEnvelope().getWidth() / gridWidth;
        final double pixelHeight = getEnvelope().getHeight() / gridHeight;
        final double x = (coordinate.lon - getEnvelope().getMinX()) / pixelWidth;
        final double y = (getEnvelope().getMaxY() - coordinate.lat) / pixelHeight;
        return new Double2D(x, y);
    }

    /** Cached: computed once per (cell, neighbourhoodSize) pair and reused thereafter. */
    @Override
    public List<Int2D> getNeighbours(
        final Int2D cell,
        @SuppressWarnings("SameParameterValue") final int neighbourhoodSize
    ) {
        return mooreNeighbourhoods.get(entry(cell, neighbourhoodSize));
    }

    /** Cached: computed once per (cell, neighbourhoodSize) pair and reused thereafter. */
    @Override
    public List<Int2D> getActiveNeighbours(
        final Int2D cell,
        @SuppressWarnings("SameParameterValue") final int neighbourhoodSize
    ) {
        return activeMooreNeighbourhoods.get(entry(cell, neighbourhoodSize));
    }

    private List<Int2D> computeActiveMooreNeighbourhood(
        final Entry<Int2D, Integer> entry
    ) {
        return mooreNeighbourhoods
            .get(entry)
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

    @Override
    public abstract boolean isActive(final Int2D cell);

}
