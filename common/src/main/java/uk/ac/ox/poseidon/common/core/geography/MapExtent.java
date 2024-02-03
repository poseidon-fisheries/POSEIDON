package uk.ac.ox.poseidon.common.core.geography;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Point;
import sim.util.Double2D;
import sim.util.Int2D;

import java.util.Objects;

import static java.lang.Math.floor;

/**
 * This is a value class that stores some essential geographic properties of a NauticalMap so that classes that are only
 * concerned with those can share instances between simulations by avoiding a dependency on the full NauticalMap.
 */
public final class MapExtent {

    // This cache looks weird but here is the deal: MapExtent is an immutable value class,
    // so it's safe to share instances and there is no point in having multiple copies
    // around. For any MapExtent object, the cache will spit back the first equivalent
    // instance that was created. The big win here is that it allows every simulation
    // with the same map extent to share the same coordinate field.
    private static final LoadingCache<MapExtent, MapExtent> cache =
        CacheBuilder.newBuilder().build(CacheLoader.from(mapExtent -> mapExtent));

    private final int gridWidth;   // the width in cells
    private final int gridHeight;  // the height in cells
    private final double cellWidth;   // the width of a cell in degrees
    private final double cellHeight;  // the height of a cell in degrees
    private final Envelope envelope;
    private final int hashCode;

    private final CoordinateField coordinateField;

    private MapExtent(
        final int gridWidth,
        final int gridHeight,
        final Envelope envelope
    ) {
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
        this.envelope = new Envelope(envelope); // The Envelope class is mutable, so we store a copy
        this.cellWidth = envelope.getWidth() / (double) this.getGridWidth();
        this.cellHeight = envelope.getHeight() / (double) this.getGridHeight();
        this.hashCode = Objects.hash(gridWidth, gridHeight, envelope);
        this.coordinateField = new CoordinateField(this);
    }

    public int getGridWidth() {
        return gridWidth;
    }

    public int getGridHeight() {
        return gridHeight;
    }

    public static MapExtent from(
        final int gridWidth,
        final int gridHeight,
        final Envelope envelope
    ) {
        return cache.getUnchecked(new MapExtent(gridWidth, gridHeight, envelope));
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final MapExtent mapExtent = (MapExtent) o;
        return gridWidth == mapExtent.gridWidth && gridHeight == mapExtent.gridHeight
            && envelope.equals(mapExtent.envelope);
    }

    public int toGridX(final double longitude) {
        return (int) floor((longitude - envelope.getMinX()) / cellWidth);
    }

    public int toGridY(final double latitude) {
        return (int) floor((envelope.getMaxY() - latitude) / cellHeight);
    }

    public Double2D coordinateToXY(final Coordinate coordinate) {
        return coordinateToXY(
            coordinate,
            getGridWidth(),
            getGridHeight(),
            getEnvelope()
        );
    }

    /**
     * Transforms a lon/lat coordinate to an x/y coordinate that can be used with a continuous field covering the same
     * space as the nautical map. This is basically a floating point version of vectors.size().toXCoord/.toYCoord; not
     * sure why it doesn't exist in GeomVectorField in the first place...
     */
    public static Double2D coordinateToXY(
        final Coordinate coordinate,
        final double gridWidth,
        final double gridHeight,
        final Envelope mbr
    ) {
        final double pixelWidth = mbr.getWidth() / gridWidth;
        final double pixelHeight = mbr.getHeight() / gridHeight;
        final double x = (coordinate.x - mbr.getMinX()) / pixelWidth;
        final double y = (mbr.getMaxY() - coordinate.y) / pixelHeight;
        return new Double2D(x, y);
    }

    /**
     * Returns a copy of the map's envelope. The Envelope class is mutable, so we don't want to expose a copy of our
     * envelope and risk it being changed under our feet.
     */
    public Envelope getEnvelope() {
        return new Envelope(envelope);
    }

    public Coordinate getCoordinates(
        final int gridX,
        final int gridY
    ) {
        return coordinateField.getCoordinate(gridX, gridY);
    }

    public Coordinate getCoordinates(final Int2D gridXY) {
        return coordinateField.getCoordinate(gridXY.x, gridXY.y);
    }

    public Point toPoint(
        final int gridX,
        final int gridY
    ) {
        return coordinateField.toPoint(gridX, gridY);
    }
}
