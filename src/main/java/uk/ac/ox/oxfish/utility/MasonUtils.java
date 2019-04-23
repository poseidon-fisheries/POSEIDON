package uk.ac.ox.oxfish.utility;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import ec.util.MersenneTwisterFast;
import org.apache.commons.collections15.set.ListOrderedSet;
import org.jetbrains.annotations.NotNull;
import sim.field.continuous.Continuous2D;
import sim.field.geo.GeomField;
import sim.field.geo.GeomGridField;
import sim.field.grid.Grid2D;
import sim.util.Bag;
import sim.util.Double2D;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.google.common.base.Predicates.not;
import static java.util.stream.Collectors.toCollection;

public class MasonUtils {

    @SuppressWarnings("unchecked")
    @NotNull
    public static <T> Stream<T> bagToStream(Bag bag) {
        return Optional.ofNullable(bag)
            .map(b -> IntStream.range(0, b.size()).mapToObj(i -> (T) b.get(i)))
            .orElse(Stream.empty());
    }

    @SuppressWarnings("unchecked")
    @NotNull
    public static <T> Optional<T> oneOf(Bag bag, MersenneTwisterFast random) {
        //noinspection ConstantConditions,Guava
        return Optional.ofNullable(bag)
            .filter(not(Bag::isEmpty))
            .map(b -> (T) b.get(random.nextInt(b.size())));
    }

    @NotNull
    public static <T> Optional<T> oneOf(Stream<T> stream, MersenneTwisterFast random) {
        final ArrayList<T> list = stream.collect(toCollection(ArrayList::new));
        return oneOf(list, random);
    }

    @NotNull
    public static <T> Optional<T> oneOf(List<T> list, MersenneTwisterFast random) {
        //noinspection ConstantConditions,Guava
        return Optional.ofNullable(list)
            .filter(not(List::isEmpty))
            .map(l -> l.get(random.nextInt(l.size())));
    }

    @NotNull
    public static <T> Optional<T> oneOf(ListOrderedSet<T> set, MersenneTwisterFast random) {
        //noinspection ConstantConditions,Guava
        return Optional.ofNullable(set)
            .filter(not(ListOrderedSet::isEmpty))
            .map(l -> l.get(random.nextInt(l.size())));
    }

    /**
     * Transforms a lon/lat coordinate to an x/y coordinate that can be used with a continuous field
     * covering the same space as the nautical map. This is basically a floating point version of
     * vectors.size().toXCoord/.toYCoord; not sure why it doesn't exist in GeomVectorField in the
     * first place...
     */
    public static Double2D coordinateToXY(GeomGridField geomGridField, Coordinate coordinate) {
        final Envelope mbr = geomGridField.getMBR();
        final double x = (coordinate.x - mbr.getMinX()) / geomGridField.getPixelWidth();
        final double y = (mbr.getMaxY() - coordinate.y) / geomGridField.getPixelHeight();
        return new Double2D(x, y);
    }

    public static boolean inBounds(Double2D location, Continuous2D continuous2D) {
        return inBounds(location, continuous2D.getWidth(), continuous2D.getHeight());
    }

    public static boolean inBounds(Double2D location, Grid2D grid2D) {
        return inBounds(location, grid2D.getWidth(), grid2D.getHeight());
    }

    private static boolean inBounds(Double2D location, double fieldWidth, double fieldHeight) {
        return location.x >= 0 && location.x < fieldWidth && location.y >= 0 && location.y < fieldHeight;
    }

    public static boolean inBounds(Double2D location, GeomField geomField) {
        return inBounds(location, geomField.getWidth(), geomField.getHeight());
    }

}
