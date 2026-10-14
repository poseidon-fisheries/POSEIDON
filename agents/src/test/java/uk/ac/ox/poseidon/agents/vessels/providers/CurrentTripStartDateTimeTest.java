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

package uk.ac.ox.poseidon.agents.vessels.providers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.agents.trips.Trip;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.core.schedule.TemporalSchedule;

class CurrentTripStartDateTimeTest {

  private static final LocalDateTime NOW = LocalDateTime.of(2026, 1, 1, 3, 0);

  private final Vessel vessel = mock(Vessel.class);

  CurrentTripStartDateTimeTest() {
    final TemporalSchedule schedule = mock(TemporalSchedule.class);
    when(schedule.getDateTime()).thenReturn(NOW);
    when(vessel.getSchedule()).thenReturn(schedule);
  }

  @Test
  void throwsWhenTheVesselIsNotOnATrip() {
    when(vessel.getCurrentTrip()).thenReturn(null);

    assertThatThrownBy(() ->
      new CurrentTripStartDateTime(vessel).get()
    ).isInstanceOf(IllegalStateException.class);
  }

  @Test
  void isTheStartOfTheCurrentTrip() {
    final Trip trip = mock(Trip.class);
    when(trip.getStartDateTime()).thenReturn(NOW.minusHours(5));
    when(vessel.getCurrentTrip()).thenReturn(trip);

    assertThat(new CurrentTripStartDateTime(vessel).get()).isEqualTo(
      NOW.minusHours(5)
    );
  }
}
