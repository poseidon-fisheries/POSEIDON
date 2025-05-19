/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

package uk.ac.ox.oxfish.geography.currents;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import sim.util.Double2D;
import sim.util.Int2D;

import java.util.EnumMap;
import java.util.Map;
import java.util.TreeMap;

import static java.util.stream.IntStream.range;
import static uk.ac.ox.oxfish.geography.currents.CurrentPattern.NEUTRAL;
import static uk.ac.ox.oxfish.geography.currents.CurrentVectorsEPO.getInterpolatedVector;

public class CurrentVectorsTest {

    @Test
    public void positiveDaysOffset() {
        Assertions.assertEquals(0, CurrentVectorsEPO.positiveDaysOffset(1, 1));
        Assertions.assertEquals(1, CurrentVectorsEPO.positiveDaysOffset(1, 2));
        Assertions.assertEquals(364, CurrentVectorsEPO.positiveDaysOffset(1, 365));
        Assertions.assertEquals(1, CurrentVectorsEPO.positiveDaysOffset(365, 1));
    }

    @Test
    public void negativeDaysOffset() {
        Assertions.assertEquals(0, CurrentVectorsEPO.negativeDaysOffset(1, 1));
        Assertions.assertEquals(-1, CurrentVectorsEPO.negativeDaysOffset(2, 1));
        Assertions.assertEquals(-364, CurrentVectorsEPO.negativeDaysOffset(365, 1));
        Assertions.assertEquals(-1, CurrentVectorsEPO.negativeDaysOffset(1, 365));
    }

    @Test
    public void getInterpolatedVectorTest() {
        final Double2D vectorBefore = new Double2D(0, 1);
        final Double2D vectorAfter = new Double2D(1, 0);
        Assertions.assertEquals(new Double2D(0, 1), getInterpolatedVector(vectorBefore, 0, vectorAfter, 4));
        Assertions.assertEquals(new Double2D(0.25, 0.75), getInterpolatedVector(vectorBefore, 1, vectorAfter, 3));
        Assertions.assertEquals(new Double2D(0.5, 0.5), getInterpolatedVector(vectorBefore, 2, vectorAfter, 2));
        Assertions.assertEquals(new Double2D(0.75, 0.25), getInterpolatedVector(vectorBefore, 3, vectorAfter, 1));
        Assertions.assertEquals(new Double2D(1, 0), getInterpolatedVector(vectorBefore, 4, vectorAfter, 0));
    }

    @Test
    public void testGetVector() {
        final Int2D gridLocation = new Int2D(0, 0);
        final TreeMap<Integer, EnumMap<CurrentPattern, Map<Int2D, Double2D>>> vectorMaps = new TreeMap<>();
        vectorMaps.put(1, new EnumMap<>(ImmutableMap.of(NEUTRAL, ImmutableMap.of(gridLocation, new Double2D(0, 0)))));
        vectorMaps.put(5, new EnumMap<>(ImmutableMap.of(NEUTRAL, ImmutableMap.of(gridLocation, new Double2D(1, 0)))));
        final CurrentVectors currentVectors = new CurrentVectorsEPO(vectorMaps, __ -> NEUTRAL, 0, 0, 1);
        final ImmutableList<Double2D> expectedVectors = ImmutableList.of(
            new Double2D(0.0, 0.0),
            new Double2D(0.25, 0.0),
            new Double2D(0.5, 0.0),
            new Double2D(0.75, 0.0),
            new Double2D(1.0, 0.0)
        );
        range(0, expectedVectors.size()).forEach(i ->
            Assertions.assertEquals(expectedVectors.get(i), currentVectors.getVector(i, gridLocation))
        );
    }
}
