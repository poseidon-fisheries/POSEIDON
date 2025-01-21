/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025 CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.poseidon.core;

import ec.util.MersenneTwisterFast;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static uk.ac.ox.poseidon.core.MasonUtils.shuffledStream;

class MasonUtilsTest {

    @Test
    void testShuffledStreamReturnsAllElements() {
        final List<String> candidates = Arrays.asList(
            "apple",
            "banana",
            "cherry",
            "date",
            "elderberry"
        );
        final MersenneTwisterFast rng = new MersenneTwisterFast();

        final List<String> result = shuffledStream(candidates, rng).toList();

        // Verify that all elements are present
        assertEquals(new HashSet<>(candidates), new HashSet<>(result));
    }

    @Test
    void testShuffledStreamHasNoDuplicates() {
        final List<String> candidates = Arrays.asList(
            "apple",
            "banana",
            "cherry",
            "date",
            "elderberry"
        );
        final MersenneTwisterFast rng = new MersenneTwisterFast();

        final List<String> result = shuffledStream(candidates, rng).toList();

        // Verify no duplicates
        assertEquals(result.size(), new HashSet<>(result).size());
    }

    @Test
    void testShuffledStreamOrderIsRandom() {
        final List<Integer> candidates = IntStream
            .range(0, 100)
            .boxed()
            .collect(Collectors.toList());
        final MersenneTwisterFast rng = new MersenneTwisterFast();

        final List<Integer> result1 = shuffledStream(candidates, rng).collect(Collectors.toList());
        final List<Integer> result2 = shuffledStream(candidates, rng).collect(Collectors.toList());

        // Verify that at least one of the orders is different
        assertNotEquals(
            result1,
            result2,
            "Randomness test failed: the streams are in the same order."
        );
    }

    @Test
    void testShuffledStreamEmptyList() {
        final List<String> candidates = Collections.emptyList();
        final MersenneTwisterFast rng = new MersenneTwisterFast();

        final List<String> result = shuffledStream(candidates, rng).toList();

        // Verify that the result is empty
        assertTrue(result.isEmpty());
    }

    @Test
    void testShuffledStreamSingleElement() {
        final List<String> candidates = Collections.singletonList("only");
        final MersenneTwisterFast rng = new MersenneTwisterFast();

        final List<String> result = shuffledStream(candidates, rng).toList();

        // Verify the single element is returned
        assertEquals(Collections.singletonList("only"), result);
    }

    @Test
    void testShuffledStreamLargeList() {
        final int size = 10_000;
        final List<Integer> candidates = IntStream
            .range(0, size)
            .boxed()
            .collect(Collectors.toList());
        final MersenneTwisterFast rng = new MersenneTwisterFast();

        final List<Integer> result = shuffledStream(candidates, rng).toList();

        // Verify that all elements are present and there are no duplicates
        assertEquals(size, result.size());
        assertEquals(new HashSet<>(candidates), new HashSet<>(result));
    }

}
