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
import static org.mockito.Mockito.when;
import static uk.ac.ox.poseidon.fishing.choices.MutableMemoryEntriesTest.contents;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.events.SimpleEventManager;
import uk.ac.ox.poseidon.fishing.trips.FishingTripEndEvent;
import uk.ac.ox.poseidon.fishing.vessels.FishingTrip;

class EventRecorderTest {

  @Test
  void skipsAnAbsentKeyBeforeReadingTheObservationMemoryOrTime() {
    final var recorder = new EventRecorder<
      FishingTripEndEvent,
      String,
      String,
      Integer
    >(
      FishingTripEndEvent.class,
      _ -> Optional.empty(),
      _ -> {
        throw new AssertionError("observation read");
      },
      _ -> {
        throw new AssertionError("memory read");
      },
      (_, _, _) -> {
        throw new AssertionError("rule called");
      }
    );
    recorder.receive(new FishingTripEndEvent(mock(FishingTrip.class)));
  }

  @Test
  void passesTheEventsTimeToTheRuleThroughAnActualEventManager() {
    final LocalDateTime time = LocalDateTime.of(2026, 1, 1, 12, 0);
    final FishingTrip trip = mock(FishingTrip.class);
    when(trip.getEndDateTime()).thenReturn(time);
    final MutableMemoryEntries<String, Integer> memory =
      new MutableMemoryEntries<>();
    final List<LocalDateTime> times = new ArrayList<>();
    final var recorder = new EventRecorder<
      FishingTripEndEvent,
      String,
      String,
      Integer
    >(
      FishingTripEndEvent.class,
      _ -> Optional.of("A"),
      _ -> "fish",
      _ -> memory,
      (remembered, observation, observationTime) -> {
        times.add(observationTime);
        return remembered.orElse(0) + observation.length();
      }
    );
    final var manager = new SimpleEventManager();
    manager.addListener(recorder);
    manager.broadcast(new FishingTripEndEvent(trip));
    manager.broadcast(new FishingTripEndEvent(trip));

    assertThat(times).containsExactly(time, time);
    assertThat(contents(memory)).containsExactly(Map.entry("A", 8));
  }
}
