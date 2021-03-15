package uk.ac.ox.oxfish.geography.currents;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import sim.util.Double2D;
import sim.util.Int2D;
import uk.ac.ox.oxfish.geography.SeaTile;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.function.Function.identity;
import static java.util.stream.IntStream.range;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static uk.ac.ox.oxfish.geography.currents.CurrentPattern.NEUTRAL;

public class DriftingPathTest {

    @Test
    public void position() {
        final ImmutableMap<Integer, SeaTile> seaTiles =
            range(0, 3).boxed().collect(toImmutableMap(
                identity(),
                x -> new SeaTile(x, 0, 0, null)
            ));

        final TreeMap<Integer, EnumMap<CurrentPattern, Map<Int2D, Double2D>>> vectorMaps = new TreeMap<>();
        final ImmutableMap<Int2D, Double2D> tileVectors =
            seaTiles.values().stream().collect(toImmutableMap(SeaTile::getGridLocation, __ -> new Double2D(1, 0)));
        vectorMaps.put(1, new EnumMap<>(ImmutableMap.of(NEUTRAL, tileVectors)));
        final CurrentVectors currentVectors = new CurrentVectors(vectorMaps, __ -> NEUTRAL, 3, 1, 1);
        final DriftingPath driftingPath = new DriftingPath(0, new Double2D(0, 0), currentVectors);

        try {
            driftingPath.position(-1);
            fail("should have thrown an IllegalArgumentException");
        } catch (IllegalArgumentException ignored) {}

        assertEquals(
            ImmutableMap.of(0, Optional.of(new Double2D(0, 0))),
            driftingPath.getPositions()
        );

        assertEquals(
            Optional.of(new Double2D(0, 0)),
            driftingPath.position(0)
        );

        assertEquals(
            ImmutableMap.of(0, Optional.of(new Double2D(0, 0))),
            driftingPath.getPositions()
        );

        assertEquals(
            Optional.of(new Double2D(1, 0)),
            driftingPath.position(1)
        );

        assertEquals(
            ImmutableMap.of(
                0, Optional.of(new Double2D(0, 0)),
                1, Optional.of(new Double2D(1, 0))
            ),
            driftingPath.getPositions()
        );

        assertEquals(
            driftingPath.position(3),
            Optional.empty()
        );

        assertEquals(
            ImmutableMap.of(
                0, Optional.of(new Double2D(0, 0)),
                1, Optional.of(new Double2D(1, 0)),
                2, Optional.of(new Double2D(2, 0)),
                3, Optional.empty()
            ),
            driftingPath.getPositions()
        );

    }
}