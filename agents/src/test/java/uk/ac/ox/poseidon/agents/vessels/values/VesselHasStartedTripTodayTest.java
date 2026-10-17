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

package uk.ac.ox.poseidon.agents.vessels.values;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.agents.vessels.Trip;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.core.schedule.TemporalSchedule;

class VesselHasStartedTripTodayTest {

  private static final LocalDateTime NOW = LocalDateTime.of(2026, 3, 10, 19, 0);

  private final VesselHasStartedTripToday vesselHasStartedTripToday =
    new VesselHasStartedTripToday();

  @Test
  void isFalseForAVesselThatHasNeverMadeATrip() {
    assertFalse(vesselHasStartedTripToday.apply(vesselWith(null, null)));
  }

  @Test
  void isTrueWhenThePreviousTripStartedToday() {
    final Trip previousTrip = tripStartedAt(NOW.withHour(7));
    assertTrue(vesselHasStartedTripToday.apply(vesselWith(null, previousTrip)));
  }

  @Test
  void isFalseWhenThePreviousTripStartedYesterday() {
    final Trip previousTrip = tripStartedAt(NOW.minusDays(1).withHour(23));
    assertFalse(
      vesselHasStartedTripToday.apply(vesselWith(null, previousTrip))
    );
  }

  @Test
  void looksAtTheCurrentTripBeforeThePreviousOne() {
    final Trip currentTrip = tripStartedAt(NOW.withHour(7));
    final Trip previousTrip = tripStartedAt(NOW.minusDays(1).withHour(7));
    assertTrue(
      vesselHasStartedTripToday.apply(vesselWith(currentTrip, previousTrip))
    );
  }

  @Test
  void isFalseWhenTheCurrentTripStartedYesterday() {
    final Trip currentTrip = tripStartedAt(NOW.minusDays(1).withHour(22));
    assertFalse(vesselHasStartedTripToday.apply(vesselWith(currentTrip, null)));
  }

  private static Trip tripStartedAt(final LocalDateTime startDateTime) {
    final Trip trip = mock(Trip.class);
    when(trip.getStartDateTime()).thenReturn(startDateTime);
    return trip;
  }

  private static Vessel vesselWith(
    final Trip currentTrip,
    final Trip previousTrip
  ) {
    final Vessel vessel = mock(Vessel.class);
    final TemporalSchedule schedule = mock(TemporalSchedule.class);
    when(schedule.getDateTime()).thenReturn(NOW);
    when(vessel.getSchedule()).thenReturn(schedule);
    when(vessel.getCurrentTrip()).thenReturn(currentTrip);
    when(vessel.getPreviousTrip()).thenReturn(previousTrip);
    return vessel;
  }
}
