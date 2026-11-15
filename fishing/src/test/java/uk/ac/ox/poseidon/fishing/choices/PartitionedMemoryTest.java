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
import static uk.ac.ox.poseidon.fishing.choices.MutableMemoryEntriesTest.contents;

import java.time.LocalDateTime;
import java.util.Map;
import org.junit.jupiter.api.Test;

class PartitionedMemoryTest {

  private static final LocalDateTime TIME = LocalDateTime.of(2026, 1, 1, 12, 0);

  @Test
  void inputsSelectingTheSamePartitionReachTheSameEntries() {
    final var memory = new PartitionedMemory<String, String, String, Double>(
      input -> input.substring(0, 1)
    );
    memory.apply("trawl").record("A", 3.0, TIME, new KeepLastReceived<>());
    memory.apply("seine").record("A", 5.0, TIME, new KeepLastReceived<>());

    assertThat(memory.apply("troll")).isSameAs(memory.apply("trawl"));
    assertThat(contents(memory.apply("troll"))).containsExactly(
      Map.entry("A", 3.0)
    );
    assertThat(contents(memory.apply("seine"))).containsExactly(
      Map.entry("A", 5.0)
    );
    assertThat(contents(memory.apply("longline"))).isEmpty();
  }

  @Test
  void nullPartitionKeysHaveIndependentEntriesToo() {
    final var memory = new PartitionedMemory<String, String, String, Double>(
      input -> input
    );
    memory.apply(null).record("A", 3.0, TIME, new KeepLastReceived<>());
    assertThat(contents(memory.apply(null))).containsExactly(
      Map.entry("A", 3.0)
    );
    assertThat(contents(memory.apply("trawl"))).isEmpty();
  }
}
