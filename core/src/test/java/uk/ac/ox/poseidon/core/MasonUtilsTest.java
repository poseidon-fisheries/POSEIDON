/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025-2026, University of Oxford.
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static uk.ac.ox.poseidon.core.MasonUtils.bagToSet;
import static uk.ac.ox.poseidon.core.MasonUtils.bagToStream;
import static uk.ac.ox.poseidon.core.MasonUtils.inBounds;
import static uk.ac.ox.poseidon.core.MasonUtils.oneOf;
import static uk.ac.ox.poseidon.core.MasonUtils.reservoirSample;
import static uk.ac.ox.poseidon.core.MasonUtils.shuffledStream;
import static uk.ac.ox.poseidon.core.MasonUtils.upToNOf;
import static uk.ac.ox.poseidon.core.MasonUtils.upToOneOf;

import ec.util.MersenneTwisterFast;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.RandomAccess;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import sim.field.continuous.Continuous2D;
import sim.field.grid.DenseGrid2D;
import sim.util.Bag;
import sim.util.Double2D;

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
  void upToNOfPreservesOriginalRelativeOrder() {
    final List<Integer> candidates = IntStream.range(0, 100).boxed().toList();

    for (long seed = 0; seed < 50; seed++) {
      final MersenneTwisterFast rng = new MersenneTwisterFast(seed);
      final List<Integer> result = upToNOf(7, candidates, rng);
      assertThat(result).isSorted();
    }
  }

  @Test
  void upToNOfSamplesUniformlyAcrossSeeds() {
    final List<Integer> candidates = IntStream.range(0, 10).boxed().toList();
    final int[] selectionCounts = new int[candidates.size()];

    final int runs = 10_000;
    for (long seed = 0; seed < runs; seed++) {
      final MersenneTwisterFast rng = new MersenneTwisterFast(seed);
      upToNOf(3, candidates, rng).forEach(i -> selectionCounts[i]++);
    }

    final double expected = (runs * 3.0) / candidates.size();
    for (final int count : selectionCounts) {
      assertThat((double) count).isCloseTo(expected, within(expected * 0.15));
    }
  }

  @Test
  void upToNOfSamplesWhenNIsOneLessThanSize() {
    final List<Integer> candidates = List.of(10, 20, 30, 40, 50);

    for (long seed = 0; seed < 50; seed++) {
      final MersenneTwisterFast rng = new MersenneTwisterFast(seed);
      final List<Integer> result = upToNOf(
        candidates.size() - 1,
        candidates,
        rng
      );
      assertThat(result).hasSize(candidates.size() - 1);
      assertThat(candidates).containsAll(result);
    }
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
  void bagToSetCollectsEveryElement() {
    final Bag bag = new Bag(List.of("a", "b"));

    assertThat(bagToSet(bag)).containsExactlyInAnyOrder("a", "b");
  }

  @Test
  void bagToSetOfANullBagIsEmpty() {
    assertThat(MasonUtils.<String>bagToSet(null)).isEmpty();
  }

  @Test
  void bagToStreamReadsEveryElement() {
    final Bag bag = new Bag(List.of("a", "b"));

    assertThat(bagToStream(bag).toList()).containsExactly("a", "b");
  }

  @Test
  void bagToStreamOfANullBagIsEmpty() {
    assertThat(MasonUtils.<String>bagToStream(null).toList()).isEmpty();
  }

  @Test
  void bagToStreamCastsEveryElementToTheGivenClass() {
    final Bag bag = new Bag(List.of("a", "b"));

    assertThat(bagToStream(bag, String.class).toList()).containsExactly(
      "a",
      "b"
    );
  }

  @Test
  void bagToStreamRefusesAnElementOfAnotherClass() {
    final Bag bag = new Bag(List.of("a", 1));

    assertThatThrownBy(() ->
      bagToStream(bag, String.class).toList()
    ).isInstanceOf(ClassCastException.class);
  }

  @Test
  void oneOfAnIteratorPicksItsOnlyElement() {
    assertThat(
      oneOf(List.of("only").iterator(), new MersenneTwisterFast(42))
    ).isEqualTo("only");
  }

  @Test
  void oneOfABagPicksItsOnlyElement() {
    assertThat(
      oneOf(new Bag(List.of("only")), new MersenneTwisterFast(42))
    ).isEqualTo("only");
  }

  @Test
  void oneOfAnArrayPicksItsOnlyElement() {
    assertThat(
      oneOf(new String[] { "only" }, new MersenneTwisterFast(42))
    ).isEqualTo("only");
  }

  @Test
  void oneOfAListPicksItsOnlyElement() {
    assertThat(oneOf(List.of("only"), new MersenneTwisterFast(42))).isEqualTo(
      "only"
    );
  }

  @Test
  void oneOfAListDrawsTheIndexTheRngPicks() {
    final List<String> candidates = List.of("a", "b", "c");

    for (long seed = 0; seed < 10; seed++) {
      final int expectedIndex = new MersenneTwisterFast(seed).nextInt(3);
      assertThat(oneOf(candidates, new MersenneTwisterFast(seed))).isEqualTo(
        candidates.get(expectedIndex)
      );
    }
  }

  @Test
  void oneOfABagDrawsTheIndexTheRngPicks() {
    final Bag candidates = new Bag(List.of("a", "b", "c"));

    for (long seed = 0; seed < 10; seed++) {
      final int expectedIndex = new MersenneTwisterFast(seed).nextInt(3);
      assertThat(oneOf(candidates, new MersenneTwisterFast(seed))).isEqualTo(
        candidates.get(expectedIndex)
      );
    }
  }

  @Test
  void oneOfAnArrayDrawsTheIndexTheRngPicks() {
    final String[] candidates = { "a", "b", "c" };

    for (long seed = 0; seed < 10; seed++) {
      final int expectedIndex = new MersenneTwisterFast(seed).nextInt(3);
      assertThat(oneOf(candidates, new MersenneTwisterFast(seed))).isEqualTo(
        candidates[expectedIndex]
      );
    }
  }

  @Test
  void oneOfRefusesAnEmptyList() {
    assertThatThrownBy(() ->
      oneOf(List.<String>of(), new MersenneTwisterFast(42))
    )
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("must not be empty");
  }

  @Test
  void oneOfRefusesAnEmptyBag() {
    final Bag empty = new Bag();

    assertThatThrownBy(() -> oneOf(empty, new MersenneTwisterFast(42)))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("must not be empty");
  }

  @Test
  void oneOfRefusesAnEmptyArray() {
    assertThatThrownBy(() -> oneOf(new String[0], new MersenneTwisterFast(42)))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("must not be empty");
  }

  @Test
  void upToOneOfIsPresentForANonEmptyList() {
    assertThat(
      upToOneOf(List.of("only"), new MersenneTwisterFast(42))
    ).contains("only");
  }

  @Test
  void upToOneOfIsEmptyForAnEmptyList() {
    assertThat(
      upToOneOf(List.<String>of(), new MersenneTwisterFast(42))
    ).isEmpty();
  }

  @Test
  void upToOneOfIsEmptyForANullList() {
    assertThat(
      MasonUtils.<String>upToOneOf(null, new MersenneTwisterFast(42))
    ).isEmpty();
  }

  @Test
  void shuffledStreamOfACollectionReturnsEveryElement() {
    final Collection<String> candidates = Set.of("apple", "banana", "cherry");

    assertThat(
      shuffledStream(candidates, new MersenneTwisterFast(42)).toList()
    ).containsExactlyInAnyOrderElementsOf(candidates);
  }

  @Test
  void shuffledStreamOfAListCollectionReturnsEveryElement() {
    final Collection<String> candidates = new ArrayList<>(
      List.of("apple", "banana", "cherry")
    );

    assertThat(
      shuffledStream(candidates, new MersenneTwisterFast(42)).toList()
    ).containsExactlyInAnyOrderElementsOf(candidates);
  }

  @Test
  void shuffledStreamOfANonRandomAccessListReturnsEveryElement() {
    final Collection<String> candidates = new LinkedList<>(
      List.of("apple", "banana", "cherry")
    );

    assertThat(
      shuffledStream(candidates, new MersenneTwisterFast(42)).toList()
    ).containsExactlyInAnyOrderElementsOf(candidates);
  }

  @Test
  void shuffledStreamOfARandomAccessCollectionReturnsEveryElement() {
    final Collection<String> candidates = new RandomAccessCollection<>(
      List.of("apple", "banana", "cherry")
    );

    assertThat(
      shuffledStream(candidates, new MersenneTwisterFast(42)).toList()
    ).containsExactlyInAnyOrderElementsOf(candidates);
  }

  @Test
  void inBoundsAcceptsALocationInsideAContinuousField() {
    assertThat(inBounds(new Double2D(5, 10), continuousField())).isTrue();
  }

  @Test
  void inBoundsRefusesALocationOutsideAContinuousField() {
    assertThat(inBounds(new Double2D(15, 10), continuousField())).isFalse();
  }

  @Test
  void inBoundsAcceptsALocationInsideAGrid() {
    assertThat(inBounds(new Double2D(5, 10), grid())).isTrue();
  }

  @Test
  void inBoundsRefusesALocationOutsideAGrid() {
    assertThat(inBounds(new Double2D(15, 10), grid())).isFalse();
  }

  @Test
  void inBoundsIncludesTheOriginButExcludesTheFarEdges() {
    final Continuous2D field = continuousField();

    assertThat(inBounds(new Double2D(0, 0), field)).isTrue();
    assertThat(inBounds(new Double2D(0, 10), field)).isTrue();
    assertThat(inBounds(new Double2D(5, 0), field)).isTrue();
    assertThat(inBounds(new Double2D(10, 10), field)).isFalse();
    assertThat(inBounds(new Double2D(5, 20), field)).isFalse();
  }

  @Test
  void inBoundsOnAGridIncludesTheOriginButExcludesTheFarEdges() {
    final DenseGrid2D grid = grid();

    assertThat(inBounds(new Double2D(0, 0), grid)).isTrue();
    assertThat(inBounds(new Double2D(0, 10), grid)).isTrue();
    assertThat(inBounds(new Double2D(5, 0), grid)).isTrue();
    assertThat(inBounds(new Double2D(10, 10), grid)).isFalse();
    assertThat(inBounds(new Double2D(5, 20), grid)).isFalse();
  }

  private static Continuous2D continuousField() {
    return new Continuous2D(1.0, 10, 20);
  }

  private static DenseGrid2D grid() {
    return new DenseGrid2D(10, 20);
  }

  private static final class RandomAccessCollection<E>
    extends AbstractCollection<E>
    implements RandomAccess
  {

    private final List<E> elements;

    private RandomAccessCollection(final List<E> elements) {
      this.elements = elements;
    }

    @Override
    public Iterator<E> iterator() {
      return elements.iterator();
    }

    @Override
    public int size() {
      return elements.size();
    }
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
