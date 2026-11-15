/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2026, University of Oxford.
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

package uk.ac.ox.poseidon.fishing.choices;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.mock;

import ec.util.MersenneTwisterFast;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;

class RandomPickerTest {

  private final Vessel vessel = mock(Vessel.class);

  @Test
  void readsCandidatesOnceAndTestsEligibilityForTheReceivingVessel() {
    final AtomicInteger reads = new AtomicInteger();
    final RandomPicker<Integer> picker = new RandomPicker<>(
      receiver -> {
        assertThat(receiver).isSameAs(vessel);
        reads.incrementAndGet();
        return List.of(1, 2);
      },
      (receiver, option) -> receiver == vessel && option == 2,
      new MersenneTwisterFast(0)
    );
    assertThat(picker.apply(vessel)).contains(2);
    assertThat(reads).hasValue(1);
  }

  @Test
  void emptyOrIneligibleCandidatesGiveNoOption() {
    assertThat(
      new RandomPicker<Integer>(
        receiver -> List.of(),
        (receiver, option) -> true,
        new MersenneTwisterFast(0)
      ).apply(vessel)
    ).isEmpty();
    assertThat(
      new RandomPicker<Integer>(
        receiver -> List.of(1, 2),
        (receiver, option) -> false,
        new MersenneTwisterFast(0)
      ).apply(vessel)
    ).isEmpty();
  }

  @Test
  void drawsUniformlyAmongEligibleCandidates() {
    final RandomPicker<Integer> picker = new RandomPicker<>(
      receiver -> List.of(0, 1, 2, 3),
      (receiver, option) -> option != 0,
      new MersenneTwisterFast(42)
    );
    final Map<Integer, Integer> counts = new HashMap<>();
    for (int i = 0; i < 10_000; i++) counts.merge(
      picker.apply(vessel).orElseThrow(),
      1,
      Integer::sum
    );
    assertThat(counts).containsOnlyKeys(1, 2, 3);
    counts
      .values()
      .forEach(count ->
        assertThat(count / 10000.0).isCloseTo(1.0 / 3, within(0.02))
      );
  }

  @Test
  void stopsTestingAfterTheFirstEligibleCandidate() {
    final AtomicInteger checks = new AtomicInteger();
    final RandomPicker<Integer> picker = new RandomPicker<>(
      receiver -> List.of(1, 2, 3),
      (receiver, option) -> {
        checks.incrementAndGet();
        return true;
      },
      new MersenneTwisterFast(42)
    );
    assertThat(picker.apply(vessel)).isPresent();
    assertThat(checks).hasValue(1);
  }

  @Test
  void theSameStableCandidateListAndSeedGiveTheSameSequence() {
    final RandomPicker<Integer> first = new RandomPicker<>(
      receiver -> List.of(3, 1, 2),
      (receiver, option) -> true,
      new MersenneTwisterFast(42)
    );
    final RandomPicker<Integer> second = new RandomPicker<>(
      receiver -> List.of(3, 1, 2),
      (receiver, option) -> true,
      new MersenneTwisterFast(42)
    );
    for (int i = 0; i < 100; i++) assertThat(first.apply(vessel)).isEqualTo(
      second.apply(vessel)
    );
  }
}
