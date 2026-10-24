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

package uk.ac.ox.poseidon.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static uk.ac.ox.poseidon.core.MasonUtils.reservoirSample;
import static uk.ac.ox.poseidon.core.MasonUtils.shuffledStream;
import static uk.ac.ox.poseidon.core.MasonUtils.upToNOf;

import ec.util.MersenneTwisterFast;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

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
    assertThat(result).containsExactlyInAnyOrderElementsOf(candidates);
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
    assertThat(result).doesNotHaveDuplicates();
  }

  @Test
  void testShuffledStreamOrderIsRandom() {
    final List<Integer> candidates = IntStream.range(0, 100)
      .boxed()
      .collect(Collectors.toList());
    final MersenneTwisterFast rng = new MersenneTwisterFast();

    final List<Integer> result1 = shuffledStream(candidates, rng).collect(
      Collectors.toList()
    );
    final List<Integer> result2 = shuffledStream(candidates, rng).collect(
      Collectors.toList()
    );

    // Verify that at least one of the orders is different
    assertThat(result2)
      .as("Randomness test failed: the streams are in the same order.")
      .isNotEqualTo(result1);
  }

  @Test
  void testShuffledStreamEmptyList() {
    final List<String> candidates = Collections.emptyList();
    final MersenneTwisterFast rng = new MersenneTwisterFast();

    final List<String> result = shuffledStream(candidates, rng).toList();

    // Verify that the result is empty
    assertThat(result).isEmpty();
  }

  @Test
  void testShuffledStreamSingleElement() {
    final List<String> candidates = Collections.singletonList("only");
    final MersenneTwisterFast rng = new MersenneTwisterFast();

    final List<String> result = shuffledStream(candidates, rng).toList();

    // Verify the single element is returned
    assertThat(result).containsExactly("only");
  }

  @Test
  void testShuffledStreamLargeList() {
    final int size = 10_000;
    final List<Integer> candidates = IntStream.range(0, size)
      .boxed()
      .collect(Collectors.toList());
    final MersenneTwisterFast rng = new MersenneTwisterFast();

    final List<Integer> result = shuffledStream(candidates, rng).toList();

    // Verify that all elements are present and there are no duplicates
    assertThat(result).containsExactlyInAnyOrderElementsOf(candidates);
  }

  @Test
  void upToNOfReturnsExactlyNElementsWhenNIsSmallerThanSize() {
    final List<Integer> candidates = List.of(10, 20, 30, 40, 50);
    final MersenneTwisterFast rng = new MersenneTwisterFast(42);
    final List<Integer> result = upToNOf(2, candidates, rng);
    assertThat(result)
      .hasSize(2)
      .isSubsetOf(candidates)
      .doesNotHaveDuplicates();
  }

  @Test
  void upToNOfReturnsAllElementsWhenNEqualsSize() {
    final List<Integer> candidates = List.of(10, 20, 30, 40, 50);
    final MersenneTwisterFast rng = new MersenneTwisterFast();
    final List<Integer> result = upToNOf(5, candidates, rng);
    assertThat(result).hasSize(5).isSubsetOf(candidates);
  }

  @Test
  void upToNOfReturnsAllElementsWhenNExceedsSize() {
    final List<Integer> candidates = List.of(10, 20, 30);
    final MersenneTwisterFast rng = new MersenneTwisterFast();
    final List<Integer> result = upToNOf(10, candidates, rng);
    assertThat(result).hasSize(3).isSubsetOf(candidates);
  }

  @Test
  void upToNOfReturnsEmptyWhenNIsZero() {
    final List<Integer> candidates = List.of(10, 20, 30);
    final MersenneTwisterFast rng = new MersenneTwisterFast();
    assertThat(upToNOf(0, candidates, rng)).isEmpty();
  }

  @Test
  void upToNOfReturnsEmptyForEmptyCandidates() {
    final MersenneTwisterFast rng = new MersenneTwisterFast();
    assertThat(upToNOf(5, List.of(), rng)).isEmpty();
  }

  @Test
  void reservoirSampleReturnsAllWhenPoolSmallerThanSampleSize() {
    final List<Integer> pool = List.of(10, 20, 30);
    final MersenneTwisterFast rng = new MersenneTwisterFast();
    final List<Integer> result = reservoirSample(pool, 10, x -> true, rng);
    assertThat(result).hasSize(3).isSubsetOf(pool);
  }

  @Test
  void reservoirSampleReturnsEmptyWhenNoItemsPassFilter() {
    final List<Integer> pool = List.of(10, 20, 30);
    final MersenneTwisterFast rng = new MersenneTwisterFast();
    assertThat(reservoirSample(pool, 5, x -> false, rng)).isEmpty();
  }

  @Test
  void reservoirSampleReturnsEmptyWhenSampleSizeIsZero() {
    final List<Integer> pool = List.of(10, 20, 30);
    final MersenneTwisterFast rng = new MersenneTwisterFast();
    assertThat(reservoirSample(pool, 0, x -> true, rng)).isEmpty();
  }

  @Test
  void reservoirSampleReturnsAllWhenPoolEqualsSampleSize() {
    final List<Integer> pool = List.of(10, 20, 30, 40, 50);
    final MersenneTwisterFast rng = new MersenneTwisterFast();
    final List<Integer> result = reservoirSample(pool, 5, x -> true, rng);
    assertThat(result).hasSize(5).isSubsetOf(pool);
  }

  @Test
  void reservoirSampleReturnsCorrectSize() {
    final List<Integer> pool = IntStream.range(0, 100).boxed().toList();
    final MersenneTwisterFast rng = new MersenneTwisterFast();
    final List<Integer> result = reservoirSample(pool, 10, x -> true, rng);
    assertThat(result).hasSize(10).isSubsetOf(pool);
  }

  @Test
  void reservoirSampleReturnsOnlyFilteredItems() {
    final List<Integer> pool = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    final MersenneTwisterFast rng = new MersenneTwisterFast();
    final List<Integer> result = reservoirSample(
      pool,
      10,
      x -> x % 2 == 0,
      rng
    );
    assertThat(result).allMatch(x -> x % 2 == 0);
  }

  @Test
  void reservoirSampleIsUniform() {
    final int poolSize = 20;
    final int sampleSize = 5;
    final int trials = 200_000;
    final double tolerance = 0.02;
    final double expectedRate = (double) sampleSize / poolSize;
    final List<Integer> pool = IntStream.range(0, poolSize).boxed().toList();
    final MersenneTwisterFast rng = new MersenneTwisterFast(123);
    final int[] counts = new int[poolSize];

    for (int t = 0; t < trials; t++) {
      for (final int item : reservoirSample(pool, sampleSize, x -> true, rng)) {
        counts[item]++;
      }
    }

    for (int i = 0; i < poolSize; i++) {
      final double rate = (double) counts[i] / trials;
      assertThat(rate)
        .as("rate of item %d", i)
        .isCloseTo(expectedRate, within(tolerance));
    }
  }
}
