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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.fishing.vessels.Trip;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;

class CurrentTimeAtSeaTest {

  private static final LocalDateTime START = LocalDateTime.of(2026, 1, 1, 6, 0);

  private final CurrentTimeAtSea currentTimeAtSea = new CurrentTimeAtSea(_ ->
    START.plusHours(5)
  );

  @Test
  void throwsWhenNoTripIsInProgress() {
    final Vessel vessel = mock(Vessel.class);
    when(vessel.getCurrentTrip()).thenReturn(null);

    assertThrows(IllegalStateException.class, () ->
      currentTimeAtSea.apply(vessel)
    );
  }

  @Test
  void isTheTripsTimeAtSeaUntilNow() {
    final Vessel vessel = mock(Vessel.class);
    final Trip trip = mock(Trip.class);
    when(vessel.getCurrentTrip()).thenReturn(trip);
    when(trip.getTimeAtSeaUntil(START.plusHours(5))).thenReturn(
      Duration.ofHours(3)
    );

    assertEquals(Duration.ofHours(3), currentTimeAtSea.apply(vessel));
  }
}
