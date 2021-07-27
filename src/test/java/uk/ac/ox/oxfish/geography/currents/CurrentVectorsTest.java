package uk.ac.ox.oxfish.geography.currents;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import sim.util.Double2D;
import sim.util.Int2D;

import java.util.EnumMap;
import java.util.Map;
import java.util.TreeMap;

import static java.util.stream.IntStream.range;
import static org.junit.Assert.assertEquals;
import static uk.ac.ox.oxfish.geography.currents.CurrentPattern.NEUTRAL;
import static uk.ac.ox.oxfish.geography.currents.CurrentVectors.getInterpolatedVector;

public class CurrentVectorsTest {

    @Test
    public void positiveDaysOffset() {
        assertEquals(0, CurrentVectors.positiveDaysOffset(1, 1));
        assertEquals(1, CurrentVectors.positiveDaysOffset(1, 2));
        assertEquals(364, CurrentVectors.positiveDaysOffset(1, 365));
        assertEquals(1, CurrentVectors.positiveDaysOffset(365, 1));
    }

    @Test
    public void negativeDaysOffset() {
        assertEquals(0, CurrentVectors.negativeDaysOffset(1, 1));
        assertEquals(-1, CurrentVectors.negativeDaysOffset(2, 1));
        assertEquals(-364, CurrentVectors.negativeDaysOffset(365, 1));
        assertEquals(-1, CurrentVectors.negativeDaysOffset(1, 365));
    }

    @Test
    public void getInterpolatedVectorTest() {
        final Double2D vectorBefore = new Double2D(0, 1);
        final Double2D vectorAfter = new Double2D(1, 0);
        assertEquals(
            new Double2D(0, 1),
            getInterpolatedVector(vectorBefore, 0, vectorAfter, 4)
        );
        assertEquals(
            new Double2D(0.25, 0.75),
            getInterpolatedVector(vectorBefore, 1, vectorAfter, 3)
        );
        assertEquals(
            new Double2D(0.5, 0.5),
            getInterpolatedVector(vectorBefore, 2, vectorAfter, 2)
        );
        assertEquals(
            new Double2D(0.75, 0.25),
            getInterpolatedVector(vectorBefore, 3, vectorAfter, 1)
        );
        assertEquals(
            new Double2D(1, 0),
            getInterpolatedVector(vectorBefore, 4, vectorAfter, 0)
        );
    }

    @Test
    public void testGetVector() {
        final Int2D gridLocation = new Int2D(0, 0);
        final TreeMap<Integer, EnumMap<CurrentPattern, Map<Int2D, Double2D>>> vectorMaps = new TreeMap<>();
        vectorMaps.put(1, new EnumMap<>(ImmutableMap.of(NEUTRAL, ImmutableMap.of(gridLocation, new Double2D(0, 0)))));
        vectorMaps.put(5, new EnumMap<>(ImmutableMap.of(NEUTRAL, ImmutableMap.of(gridLocation, new Double2D(1, 0)))));
        final CurrentVectors currentVectors = new CurrentVectors(vectorMaps, __ -> NEUTRAL, 0, 0, 1);
        final ImmutableList<Double2D> expectedVectors = ImmutableList.of(
            new Double2D(0.0, 0.0),
            new Double2D(0.25, 0.0),
            new Double2D(0.5, 0.0),
            new Double2D(0.75, 0.0),
            new Double2D(1.0, 0.0)
        );
        range(0, expectedVectors.size()).forEach(i ->
            assertEquals(expectedVectors.get(i), currentVectors.getVector(i, gridLocation))
        );
    }
}