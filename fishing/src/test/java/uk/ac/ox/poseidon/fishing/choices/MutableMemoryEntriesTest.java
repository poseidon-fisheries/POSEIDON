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
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class MutableMemoryEntriesTest {

  private static final LocalDateTime TIME = LocalDateTime.of(2026, 1, 1, 12, 0);

  @Test
  void everyObservationReachesTheRuleWithItsTimeIncludingTheFirst() {
    final MutableMemoryEntries<String, Integer> entries =
      new MutableMemoryEntries<>();
    final List<Optional<Integer>> previousValues = new ArrayList<>();
    final List<LocalDateTime> times = new ArrayList<>();
    final UpdateRule<String, Integer> rule = (
      remembered,
      observation,
      time
    ) -> {
      previousValues.add(remembered);
      times.add(time);
      return remembered.orElse(0) + observation.length();
    };

    entries.record("A", "fish", TIME, rule);
    entries.record("A", "cod", TIME.minusDays(1), rule);

    assertThat(previousValues).containsExactly(
      Optional.empty(),
      Optional.of(4)
    );
    assertThat(times).containsExactly(TIME, TIME.minusDays(1));
    assertThat(contents(entries)).containsExactly(Map.entry("A", 7));
  }

  @Test
  void visitsEntriesInFirstObservedOrderAndDoesNotMoveAnUpdatedKey() {
    final MutableMemoryEntries<String, Double> entries =
      new MutableMemoryEntries<>();
    final KeepLastReceived<Double> rule = new KeepLastReceived<>();
    entries.record("B", 2.0, TIME, rule);
    entries.record("A", 1.0, TIME, rule);
    entries.record("B", 3.0, TIME.minusDays(1), rule);

    assertThat(contents(entries)).containsExactly(
      Map.entry("B", 3.0),
      Map.entry("A", 1.0)
    );
  }

  @Test
  void rejectsNullRuleResultsForNewAndExistingKeysWithoutChangingEntries() {
    final MutableMemoryEntries<String, Integer> entries =
      new MutableMemoryEntries<>();
    assertThatNullPointerException().isThrownBy(() ->
      entries.record(
        "A",
        "first",
        TIME,
        (remembered, observation, time) -> null
      )
    );
    assertThat(contents(entries)).isEmpty();
    entries.record("A", 5, TIME, new KeepLastReceived<>());

    assertThatNullPointerException().isThrownBy(() ->
      entries.record(
        "A",
        "second",
        TIME,
        (remembered, observation, time) -> null
      )
    );
    assertThat(contents(entries)).containsExactly(Map.entry("A", 5));
  }

  @Test
  void recordersCanApplyDifferentRulesToTheSameEntries() {
    final MutableMemoryEntries<String, Double> entries =
      new MutableMemoryEntries<>();
    entries.record("A", 8.0, TIME, new KeepLastReceived<>());
    entries.record(
      "A",
      2.0,
      TIME,
      (remembered, observation, time) -> remembered.orElseThrow() + observation
    );

    assertThat(contents(entries)).containsExactly(Map.entry("A", 10.0));
  }

  static <K, M> Map<K, M> contents(final MemoryEntries<K, M> entries) {
    final Map<K, M> result = new LinkedHashMap<>();
    entries.forEachEntry(result::put);
    return result;
  }
}
