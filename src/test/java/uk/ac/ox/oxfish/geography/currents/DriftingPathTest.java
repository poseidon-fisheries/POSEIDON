package uk.ac.ox.oxfish.geography.currents;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import sim.util.Double2D;
import uk.ac.ox.oxfish.geography.SeaTile;

import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.BiFunction;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.function.Function.identity;
import static java.util.stream.IntStream.range;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class DriftingPathTest {

    @Test
    public void position() {
        final ImmutableMap<Integer, SeaTile> seaTiles =
            range(0, 3).boxed().collect(toImmutableMap(
                identity(),
                x -> new SeaTile(x, 0, 0, null)
            ));

        final DriftingPath driftingPath = new DriftingPath(0, new Double2D(0, 0));
        final TreeMap<Integer, Map<CurrentPattern, Map<SeaTile, Double2D>>> vectorMaps = new TreeMap<>();
        final ImmutableMap<SeaTile, Double2D> tileVectors =
            seaTiles.values().stream().collect(toImmutableMap(identity(), __ -> new Double2D(1, 0)));
        vectorMaps.put(1, ImmutableMap.of(CurrentPattern.NEUTRAL, tileVectors));
        final CurrentVectors currentVectors = new CurrentVectors(vectorMaps, 1);

        BiFunction<Integer, Integer, SeaTile> getSeaTile = (x, y) -> seaTiles.get(x);

        try {
            driftingPath.position(-1, currentVectors, getSeaTile);
            fail("should have thrown an IllegalArgumentException");
        } catch (IllegalArgumentException ignored) {}

        assertEquals(driftingPath.getPositions(), ImmutableMap.of(
            0, Optional.of(new Double2D(0, 0)))
        );

        assertEquals(
            driftingPath.position(0, currentVectors, getSeaTile),
            Optional.of(new Double2D(0, 0))
        );

        assertEquals(driftingPath.getPositions(), ImmutableMap.of(
            0, Optional.of(new Double2D(0, 0)))
        );

        assertEquals(
            driftingPath.position(1, currentVectors, getSeaTile),
            Optional.of(new Double2D(1, 0))
        );

        assertEquals(driftingPath.getPositions(), ImmutableMap.of(
            0, Optional.of(new Double2D(0, 0)),
            1, Optional.of(new Double2D(1, 0)))
        );

        assertEquals(
            driftingPath.position(3, currentVectors, getSeaTile),
            Optional.of(new Double2D(3, 0))
        );

        assertEquals(driftingPath.getPositions(), ImmutableMap.of(
            0, Optional.of(new Double2D(0, 0)),
            1, Optional.of(new Double2D(1, 0)),
            2, Optional.of(new Double2D(2, 0)),
            3, Optional.of(new Double2D(3, 0)))
        );

        assertEquals(
            driftingPath.position(4, currentVectors, getSeaTile),
            Optional.empty()
        );

        assertEquals(driftingPath.getPositions(), ImmutableMap.of(
            0, Optional.of(new Double2D(0, 0)),
            1, Optional.of(new Double2D(1, 0)),
            2, Optional.of(new Double2D(2, 0)),
            3, Optional.of(new Double2D(3, 0)),
            4, Optional.empty()
        ));

    }
}