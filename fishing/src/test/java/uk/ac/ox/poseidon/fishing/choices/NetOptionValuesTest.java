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
import static org.mockito.Mockito.mock;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;

class NetOptionValuesTest {

  private final Vessel vessel = mock(Vessel.class);

  @Test
  void visitsOnceAndSubtractsCostOnceOnlyForOptionsWithEvidence() {
    final AtomicInteger visits = new AtomicInteger();
    final AtomicInteger snapshots = new AtomicInteger();
    final AtomicInteger costs = new AtomicInteger();
    final AtomicReference<Double> rate = new AtomicReference<>(2.0);
    final OptionValues<String> evidence = (receiver, consumer) -> {
      assertThat(receiver).isSameAs(vessel);
      visits.incrementAndGet();
      consumer.accept("known", 10);
    };
    final Cost<String> cost = receiver -> {
      assertThat(receiver).isSameAs(vessel);
      snapshots.incrementAndGet();
      final double value = rate.get();
      return option -> {
        costs.incrementAndGet();
        return value;
      };
    };
    final NetOptionValues<String> values = new NetOptionValues<>(
      evidence,
      cost
    );
    final Map<String, Double> result = new LinkedHashMap<>();
    values.forEachEntry(vessel, result::put);
    assertThat(result).containsExactly(Map.entry("known", 8.0));
    rate.set(4.0);
    values.forEachEntry(vessel, result::put);
    assertThat(result).containsExactly(Map.entry("known", 6.0));
    assertThat(visits).hasValue(2);
    assertThat(snapshots).hasValue(2);
    assertThat(costs).hasValue(2);
  }

  @Test
  void aKnownCostDoesNotCreateAnOptionWithoutEvidence() {
    final NetOptionValues<String> values = new NetOptionValues<>(
      (receiver, consumer) -> {},
      receiver -> option -> {
        throw new AssertionError("costed an absent option");
      }
    );
    values.forEachEntry(vessel, (option, value) -> {
      throw new AssertionError("invented an option");
    });
  }
}
