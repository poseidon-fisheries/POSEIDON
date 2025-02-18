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
import com.google.common.collect.Streams;
import ec.util.MersenneTwisterFast;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import sim.field.grid.Grid2D;
import sim.field.grid.SparseGrid2D;
import sim.util.Double2D;
import sim.util.Int2D;
import sim.util.IntBag;
import sim.util.Number2D;
import uk.ac.ox.poseidon.geography.Coordinate;
import uk.ac.ox.poseidon.geography.Envelope;

import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Map.entry;
import static java.util.function.Function.identity;
import static java.util.stream.IntStream.range;

@Data
public final class ModelGrid {

    private final int gridWidth;   // the width in cells
    private final int gridHeight;  // the height in cells
    private final double cellWidth;   // the width of a cell in degrees
    private final double cellHeight;  // the height of a cell in degrees
    private final Envelope envelope;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private final LoadingCache<Entry<Int2D, Integer>, List<Int2D>> mooreNeighbourhoods =
        CacheBuilder.newBuilder().build(CacheLoader.from(this::computeMooreNeighbourhood));

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Getter(lazy = true)
    private final List<Int2D> allCells =
        range(0, getGridWidth())
            .mapToObj(x ->
                range(0, getGridHeight())
                    .mapToObj(y -> new Int2D(x, y))
            )
            .flatMap(identity())
            .toList();

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Getter(lazy = true)
    private final List<Coordinate> allCoordinates =
        getAllCells().stream().map(this::toCoordinate).toList();

    ModelGrid(
        final int gridWidth,
        final int gridHeight,
        final Envelope envelope
    ) {
        checkArgument(gridWidth > 0);
        checkArgument(gridHeight > 0);
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
        this.envelope = new Envelope(envelope); // The Envelope class is mutable, so we store a copy
        this.cellWidth = envelope.getWidth() / (double) this.getGridWidth();
        this.cellHeight = envelope.getHeight() / (double) this.getGridHeight();
    }

    /**
     * Dispatches the correct method to convert a Number2D to a coordinate. Note that, in the case
     * of Int2D, e.g. (3, 2), we assume that we are referring to the cell itself and return the
     * coordinate at the centre of the cell (3.5, 2.5) instead of at its corner (3.0, 2.0). There is
     * potential for confusion, so be careful.
     */
    public Coordinate toCoordinate(final Number2D number2D) {
        return switch (number2D) {
            case final Int2D cell -> toCoordinate(toPoint(cell));
            case final Double2D point -> toCoordinate(point);
            default -> throw new IllegalStateException("Unexpected value: " + number2D);
        };
    }

    public Coordinate toCoordinate(final Int2D cell) {
        return toCoordinate(toPoint(cell));
    }

    public Coordinate toCoordinate(final Double2D point) {
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
        return Streams.zip(
            Arrays.stream(xPositions.toArray()).boxed(),
            Arrays.stream(yPositions.toArray()).boxed(),
            Int2D::new
        ).toList();
    }

    public Int2D randomCell(final MersenneTwisterFast rng) {
        final int x = rng.nextInt(getGridWidth());
        final int y = rng.nextInt(getGridHeight());
        return new Int2D(x, y);
    }

    public double[][] makeDoubleArray() {
        return new double[getGridWidth()][getGridHeight()];
    }

    public List<Int2D> getNeighbours(
        final Int2D cell
    ) {
        return getNeighbours(cell, 1);
    }

    public boolean inGrid(final Coordinate coordinate) {
        return inGrid(toCell(coordinate));
    }

    public boolean inGrid(final Double2D point) {
        return inGrid(toCell(point));
    }

    public boolean inGrid(final Int2D cell) {
        return cell.x >= 0 && cell.y >= 0 && cell.x < gridWidth && cell.y < gridHeight;
    }
}

