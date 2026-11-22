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
 * Broadcast when a {@link Vessel} that was docked at a port leaves the
 * simulated area: its home port stopped being a modelled port, or it exits. It
 * goes on the vessel's own event manager, which forwards it to its fleet's. It
 * is not a {@link DockingEvent} or an {@link UndockingEvent}, so trip and
 * effort listeners never read it as an arrival or a departure.
 */
@Value
public class BecameAbsentEvent implements HasVessel {

  /** The vessel that became absent. */
  Vessel vessel;
  /** The port it was docked at. */
  Port port;
  /** When it became absent. */
  LocalDateTime dateTime;
}
