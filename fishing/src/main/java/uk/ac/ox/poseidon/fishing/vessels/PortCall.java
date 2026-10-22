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

package uk.ac.ox.poseidon.fishing.vessels;

import java.time.Duration;
import java.time.LocalDateTime;
import lombok.NonNull;
import uk.ac.ox.poseidon.geography.ports.Port;

/**
 * A stop of a {@link Trip} before its end: the vessel docked at {@code port}
 * and undocked again. The docking that ends a trip is its arrival, not a port
 * call.
 *
 * @param port              the port the vessel docked at
 * @param arrivalDateTime   when it docked
 * @param departureDateTime when it undocked
 */
public record PortCall(
  @NonNull Port port,
  @NonNull LocalDateTime arrivalDateTime,
  @NonNull LocalDateTime departureDateTime
) {
  /** @return the time from the arrival to the departure */
  public Duration duration() {
    return Duration.between(arrivalDateTime, departureDateTime);
  }
}
