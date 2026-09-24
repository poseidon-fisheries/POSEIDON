/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

package uk.ac.ox.poseidon.agents.vessels.engines;

import lombok.Value;

/** An {@link Engine} with a fixed cruising speed and fuel consumption rate. */
@Value
public class SimpleEngine implements Engine {
    /** The tank this engine draws fuel from. */
    FuelTank fuelTank;
    /** This engine's cruising speed, in kilometres per hour. */
    double cruisingSpeedInKph;
    /** This engine's fuel consumption rate, in litres per kilometre travelled. */
    double litresOfFuelConsumedPerKm;
}
