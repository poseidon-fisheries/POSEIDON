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

import uk.ac.ox.poseidon.geography.ports.Port;

/**
 * Where a vessel is: exactly one of absent from the simulated area, docked at
 * a port, or at sea. An absent vessel is neither docked nor at sea: it is off
 * the vessel field, and has no position.
 */
public sealed interface Whereabouts {
  /** The vessel is not in the simulated area, so it is not on the field. */
  record Absent() implements Whereabouts {}

  /**
   * The vessel is docked at {@code port}, which it holds.
   *
   * @param port the port the vessel is docked at
   */
  record Docked(Port port) implements Whereabouts {}

  /** The vessel is in the simulated area, away from any port. */
  record AtSea() implements Whereabouts {}
}
