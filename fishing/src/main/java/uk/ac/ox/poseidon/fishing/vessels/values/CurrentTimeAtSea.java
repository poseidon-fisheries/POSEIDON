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

import static com.google.common.base.Preconditions.checkState;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.function.Function;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import uk.ac.ox.poseidon.fishing.vessels.Trip;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;

/**
 * Provides the time at sea of a vessel's current trip so far: from its start to
 * now while the vessel is at sea, and its
 * {@linkplain Trip#getTotalTimeAtSea() total time at sea} once it has arrived
 * in port. Only checks made during a trip read it.
 */
@RequiredArgsConstructor
public class CurrentTimeAtSea implements Function<Vessel, Duration> {

  /** The clock, which gives the time at sea so far. */
  @NonNull
  private final Function<? super Vessel, LocalDateTime> currentDateTime;

  /**
   * @return the current trip's time at sea so far
   * @throws IllegalStateException if the vessel is not on a trip
   */
  @Override
  public Duration apply(final Vessel vessel) {
    final Trip currentTrip = vessel.getCurrentTrip();
    checkState(
      currentTrip != null,
      "Vessel %s is not on a trip.",
      vessel.getId()
    );
    return currentTrip.hasArrivedInPort()
      ? currentTrip.getTotalTimeAtSea()
      : Duration.between(
          currentTrip.getStartDateTime(),
          currentDateTime.apply(vessel)
        );
  }
}
