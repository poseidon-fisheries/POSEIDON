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

package uk.ac.ox.poseidon.fishing.vessels.values;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.events.EventManager;
import uk.ac.ox.poseidon.core.events.SimpleEventManager;
import uk.ac.ox.poseidon.fishing.trips.FishingTripStartEvent;
import uk.ac.ox.poseidon.fishing.trips.TripStartEvent;
import uk.ac.ox.poseidon.fishing.vessels.FishingTrip;
import uk.ac.ox.poseidon.fishing.vessels.Trip;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;

class HasStartedFishingTripTodayTest {

  private static final LocalDateTime NOW = LocalDateTime.of(2026, 3, 10, 19, 0);

  private final Vessel vessel = mock(Vessel.class);
  private final HasStartedFishingTripToday hasStartedFishingTripToday =
    new HasStartedFishingTripToday(_ -> NOW);
  private final EventManager eventManager = new SimpleEventManager();

  {
    eventManager.addListener(hasStartedFishingTripToday);
  }

  @Test
  void isFalseForAVesselThatHasStartedNoFishingTrip() {
    assertThat(hasStartedFishingTripToday.apply(vessel)).isFalse();
  }

  @Test
  void isTrueWhenTheLastFishingTripStartedToday() {
    startFishingTrip(NOW.withHour(7));
    assertThat(hasStartedFishingTripToday.apply(vessel)).isTrue();
  }

  @Test
  void isFalseWhenTheLastFishingTripStartedYesterday() {
    startFishingTrip(NOW.minusDays(1).withHour(23));
    assertThat(hasStartedFishingTripToday.apply(vessel)).isFalse();
  }

  @Test
  void readsTheLastFishingTripStarted() {
    startFishingTrip(NOW.withHour(7));
    startFishingTrip(NOW.plusDays(1).withHour(7));
    assertThat(hasStartedFishingTripToday.apply(vessel)).isFalse();
  }

  @Test
  void ignoresTripsOfOtherKinds() {
    final Trip relocation = mock(Trip.class);
    when(relocation.getStartDateTime()).thenReturn(NOW.withHour(7));
    eventManager.broadcast(new TripStartEvent(relocation));
    assertThat(hasStartedFishingTripToday.apply(vessel)).isFalse();
  }

  private void startFishingTrip(final LocalDateTime startDateTime) {
    final FishingTrip fishingTrip = mock(FishingTrip.class);
    when(fishingTrip.getStartDateTime()).thenReturn(startDateTime);
    eventManager.broadcast(new FishingTripStartEvent(fishingTrip));
  }
}
