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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;

class MemoryBasedOptionValuesTest {

  private final Vessel vessel = mock(Vessel.class);
  private final MutableMemoryEntries<String, Double> memory =
    new MutableMemoryEntries<>();
  private final AtomicReference<Double> scale = new AtomicReference<>(2.0);
  private final AtomicInteger snapshots = new AtomicInteger();
  private final AtomicInteger memoryReads = new AtomicInteger();
  private final MemoryBasedOptionValues<String, Double> values =
    new MemoryBasedOptionValues<>(
      pickedFor -> {
        memoryReads.incrementAndGet();
        return memory;
      },
      pickedFor -> {
        snapshots.incrementAndGet();
        final double multiplier = scale.get();
        return outcome -> outcome * multiplier;
      }
    );

  private void remember(final String option, final double value) {
    memory.record(
      option,
      value,
      LocalDateTime.of(2026, 1, 1, 0, 0),
      new KeepLastReceived<>()
    );
  }

  private Map<String, Double> visit() {
    final Map<String, Double> result = new LinkedHashMap<>();
    values.forEachEntry(vessel, result::put);
    return result;
  }

  @Test
  void readsMemoryAndCircumstancesOncePerVisit() {
    remember("A", 3);
    remember("B", 4);
    assertThat(visit()).containsExactly(
      Map.entry("A", 6.0),
      Map.entry("B", 8.0)
    );
    assertThat(memoryReads).hasValue(1);
    assertThat(snapshots).hasValue(1);
    scale.set(10.0);
    assertThat(visit()).containsExactly(
      Map.entry("A", 30.0),
      Map.entry("B", 40.0)
    );
    assertThat(snapshots).hasValue(2);
  }

  @Test
  void readsCircumstancesAtTheStartOfTheVisit() {
    remember("A", 3);
    remember("B", 4);
    final Map<String, Double> result = new LinkedHashMap<>();
    values.forEachEntry(vessel, (option, value) -> {
      result.put(option, value);
      scale.set(100.0);
    });
    assertThat(result).containsExactly(
      Map.entry("A", 6.0),
      Map.entry("B", 8.0)
    );
  }

  @Test
  void emptyMemoryVisitsNoOption() {
    assertThat(visit()).isEmpty();
    assertThat(snapshots).hasValue(1);
  }

  @Test
  void emptyMemoryStillThrowsWhenCircumstancesCannotBeRead() {
    final MemoryBasedOptionValues<String, Double> unvaluable =
      new MemoryBasedOptionValues<>(
        pickedFor -> memory,
        pickedFor -> {
          throw new IllegalStateException("no home port");
        }
      );
    assertThatThrownBy(() ->
      unvaluable.forEachEntry(vessel, (option, value) -> {})
    ).isInstanceOf(IllegalStateException.class);
  }

  @Test
  void selectsTheReceivingVesselsPartitionOnEachVisit() {
    final Vessel otherVessel = mock(Vessel.class);
    final AtomicReference<String> gear = new AtomicReference<>("trawl");
    final PartitionedMemory<Vessel, String, String, Double> partitions =
      new PartitionedMemory<>(pickedFor ->
        pickedFor == vessel ? gear.get() : "seine"
      );
    final LocalDateTime time = LocalDateTime.of(2026, 1, 1, 0, 0);
    partitions.apply(vessel).record("A", 1.0, time, new KeepLastReceived<>());
    partitions
      .apply(otherVessel)
      .record("B", 2.0, time, new KeepLastReceived<>());
    final MemoryBasedOptionValues<String, Double> partitionedValues =
      new MemoryBasedOptionValues<>(
        partitions,
        pickedFor -> outcome -> pickedFor == vessel ? outcome : outcome * 10
      );
    final Map<String, Double> result = new LinkedHashMap<>();
    partitionedValues.forEachEntry(vessel, result::put);
    assertThat(result).containsExactly(Map.entry("A", 1.0));
    result.clear();
    partitionedValues.forEachEntry(otherVessel, result::put);
    assertThat(result).containsExactly(Map.entry("B", 20.0));
    result.clear();
    gear.set("seine");
    partitionedValues.forEachEntry(vessel, result::put);
    assertThat(result).containsExactly(Map.entry("B", 2.0));
  }
}
