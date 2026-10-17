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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.agents.trips.Trip;
import uk.ac.ox.poseidon.agents.vessels.Vessel;

class CurrentTimeAtSeaTest {

  @Test
  void throwsWhenNoTripIsInProgress() {
    final Vessel vessel = mock(Vessel.class);
    when(vessel.getCurrentTrip()).thenReturn(null);

    final CurrentTimeAtSea currentTimeAtSea = new CurrentTimeAtSea(vessel);

    assertThrows(IllegalStateException.class, () ->
      currentTimeAtSea.apply(null)
    );
  }

  @Test
  void isTheCurrentTripsTimeAtSea() {
    final Vessel vessel = mock(Vessel.class);
    final Trip trip = mock(Trip.class);

    when(vessel.getCurrentTrip()).thenReturn(trip);
    when(trip.getTimeAtSea()).thenReturn(Duration.ofHours(3));

    final CurrentTimeAtSea currentTimeAtSea = new CurrentTimeAtSea(vessel);

    assertEquals(Duration.ofHours(3), currentTimeAtSea.apply(null));
  }
}
