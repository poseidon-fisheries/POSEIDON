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

import java.time.LocalDateTime;
import lombok.Value;
import uk.ac.ox.poseidon.geography.ports.Port;

/**
 * Broadcast when a {@link Vessel} docks at a port. It goes on the event manager
 * of the vessel's current trip, which forwards it to the vessel's own, or
 * straight on the vessel's own if it is on no trip.
 */
@Value
public class DockingEvent {

  /** The vessel that docked. */
  Vessel vessel;
  /** The port it docked at. */
  Port port;
  /** When it docked. */
  LocalDateTime dateTime;
}
