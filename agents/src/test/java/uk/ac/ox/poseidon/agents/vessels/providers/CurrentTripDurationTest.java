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

import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.agents.trips.Trip;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.core.schedule.TemporalSchedule;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CurrentTripDurationTest {

    @Test
    void isZeroWhenNoTripIsInProgress() {
        final Vessel vessel = mock(Vessel.class);
        when(vessel.getCurrentTrip()).thenReturn(null);

        final CurrentTripDuration currentTripDuration = new CurrentTripDuration(vessel);

        assertEquals(Duration.ZERO, currentTripDuration.get());
    }

    @Test
    void isElapsedTimeSinceTripStartWhenATripIsInProgress() {
        final Vessel vessel = mock(Vessel.class);
        final Trip trip = mock(Trip.class);
        final TemporalSchedule schedule = mock(TemporalSchedule.class);
        final LocalDateTime start = LocalDateTime.of(2026, 1, 1, 0, 0);

        when(vessel.getCurrentTrip()).thenReturn(trip);
        when(trip.getStartDateTime()).thenReturn(start);
        when(vessel.getSchedule()).thenReturn(schedule);
        when(schedule.getDateTime()).thenReturn(start.plusHours(3));

        final CurrentTripDuration currentTripDuration = new CurrentTripDuration(vessel);

        assertEquals(Duration.ofHours(3), currentTripDuration.get());
    }
}
