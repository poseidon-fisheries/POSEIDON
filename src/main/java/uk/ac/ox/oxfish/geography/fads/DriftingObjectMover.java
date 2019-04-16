package uk.ac.ox.oxfish.geography.fads;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import java.util.Map;
import java.util.function.Function;
import sim.util.Double2D;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;

public class DriftingObjectMover implements Function<Double2D, Double2D> {

    private static final Double2D NULL_VECTOR = new Double2D(0, 0);
    private final Map<SeaTile, Double2D> currentVectorMap;
    private NauticalMap nauticalMap;

    DriftingObjectMover(
        NauticalMap nauticalMap,
        Map<SeaTile, Double2D> currentVectorMap
    ) {
        this.nauticalMap = nauticalMap;
        this.currentVectorMap = currentVectorMap;
        System.out.println(currentVectorMap);
    }
    /**
     * Transforms a lon/lat coordinate to an x/y coordinate that can be used with a continuous field
     * covering the same space as the nautical map. This is basically a floating point version of
     * GeomGridField.toXCoord/.toYCoord; not sure why it doesn't exist in GeomVectorField in the
     * first place. We might consider moving this method to NauticalMap if it turns out to be
     * useful.
     */
    public static Double2D coordinatesToXY(NauticalMap nauticalMap, Coordinate coord) {
        final Envelope mbr = nauticalMap.getRasterBathymetry().getMBR();
        final double w = nauticalMap.getRasterBathymetry().getPixelWidth();
        final double h = nauticalMap.getRasterBathymetry().getPixelHeight();
        final double x = (coord.x - mbr.getMinX()) / w;
        final double y = (mbr.getMaxY() - coord.y) / h;
        return new Double2D(x, y);
    }
    private Double2D coordinatesToXY(Coordinate coord) {
        return coordinatesToXY(nauticalMap, coord);
    }
    public Double2D apply(Double2D xy, SeaTile seaTile) {
        Double2D uv = currentVectorMap.getOrDefault(seaTile, NULL_VECTOR);
        return xy.add(uv);
    }

    /**
     * This applies the current vector directly to a lon/lat coordinate, and is currently only used
     * for rough validation of FAD trajectories.
     */
    public Coordinate apply(Coordinate coord) {
        final SeaTile seaTile = nauticalMap.getSeaTile(coord);
        final Double2D xy = apply(new Double2D(coord.x, coord.y), seaTile);
        return new Coordinate(xy.x, xy.y);
    }

    @Override public Double2D apply(Double2D xy) {
        final SeaTile seaTile = nauticalMap.getSeaTile((int) xy.x, (int) xy.y);
        return apply(xy, seaTile);
    }
}
