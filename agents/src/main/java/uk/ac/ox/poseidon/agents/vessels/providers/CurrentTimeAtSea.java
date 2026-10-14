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

import static com.google.common.base.Preconditions.checkState;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import uk.ac.ox.poseidon.agents.trips.Trip;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.core.providers.Provider;

/**
 * Provides the time at sea of a vessel's current trip
 * ({@link Trip#getTimeAtSea()}); only checks made during a trip read it.
 */
@RequiredArgsConstructor
public class CurrentTimeAtSea implements Provider<Duration> {

  private final Vessel vessel;

  /**
   * @return the current trip's time at sea
   * @throws IllegalStateException if the vessel is not on a trip
   */
  @Override
  public Duration get() {
    final Trip currentTrip = vessel.getCurrentTrip();
    checkState(
      currentTrip != null,
      "Vessel %s is not on a trip.",
      vessel.getId()
    );
    return currentTrip.getTimeAtSea();
  }
}
