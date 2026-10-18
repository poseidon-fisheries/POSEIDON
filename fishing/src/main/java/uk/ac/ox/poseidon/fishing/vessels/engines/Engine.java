/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025-2026, University of Oxford.
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

package uk.ac.ox.poseidon.fishing.vessels.engines;

/**
 * A vessel's propulsion: a fixed cruising speed and fuel consumption rate. The
 * fuel itself is in a
 * {@link uk.ac.ox.poseidon.fishing.vessels.tanks.FuelTank}, a component of its
 * own, so that a new engine keeps the fuel.
 */
public interface Engine {
  /** @return this engine's cruising speed, in kilometres per hour */
  double getCruisingSpeedInKph();

  /**
   * @return this engine's fuel consumption rate, in litres per kilometre
   *         travelled
   */
  double getLitresOfFuelConsumedPerKm();
}
