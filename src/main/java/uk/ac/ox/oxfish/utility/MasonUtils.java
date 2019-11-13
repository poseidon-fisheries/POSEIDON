package uk.ac.ox.oxfish.utility;

import com.google.common.collect.ImmutableSet;
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

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.ToDoubleFunction;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

public class MasonUtils {

    @SuppressWarnings("unchecked")
    @NotNull
    public static <T> Stream<T> bagToStream(Bag bag) {
        return Optional.ofNullable(bag)
            .map(b -> IntStream.range(0, b.size()).mapToObj(i -> (T) b.get(i)))
            .orElse(Stream.empty());
    }

    @NotNull
    public static <T> ImmutableSet<T> bagToSet(Bag bag) {
        return MasonUtils.<T>bagToStream(bag).collect(toImmutableSet());
    }

    @NotNull
    public static Object oneOf(Bag candidates, MersenneTwisterFast random) {
        //noinspection unchecked
        validateCandidates(candidates);
        return candidates.get(oneOfIndices(candidates, random));
    }

    @NotNull
    public static <T> T oneOf(List<T> candidates, MersenneTwisterFast random) {
        return candidates.get(oneOfIndices(candidates, random));
    }

    @NotNull
    public static <T> T oneOf(ListOrderedSet<T> candidates, MersenneTwisterFast random) {
        return candidates.get(oneOfIndices(candidates, random));
    }

    private static int oneOfIndices(Collection candidates, MersenneTwisterFast random) {
        //noinspection unchecked
        validateCandidates(candidates);
        final int n = candidates.size();
        return n == 1 ? 0 : random.nextInt(n);
    }

    @NotNull
    public static <T> T weightedOneOf(
        List<T> candidates,
        ToDoubleFunction<T> weightFunction,
        MersenneTwisterFast random
    ) {
        validateCandidates(candidates);
        if (candidates.size() == 1) return candidates.get(0);

        final double[] weights = candidates.stream().mapToDouble(weightFunction).toArray();
        checkArgument(DoubleStream.of(weights).allMatch(w -> w >= 0));
        final double sum = DoubleStream.of(weights).sum();
        if (sum == 0) return oneOf(candidates, random);

        final List<Double> probabilities = stream(weights).mapToObj(x -> x / sum).collect(toList());
        final AliasMethod aliasMethod = new AliasMethod(probabilities, random);
        return candidates.get(aliasMethod.next());
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

    private static <E> void validateCandidates(Collection<E> candidates) {
        checkNotNull(candidates, "collection of candidates must not be null");
        checkArgument(!candidates.isEmpty(), "collection of must not be empty");
    }

}
