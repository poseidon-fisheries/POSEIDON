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

/**
 * Values of a {@link uk.ac.ox.poseidon.agents.vessels.Vessel}: functions given
 * the vessel to read. They read its state (its cell, home port cell, current
 * trip's destination, start, time at sea and event manager, its own event
 * manager, the water cells reachable from its cell), test it (whether it is
 * active, has started a trip today, is at a cell) or derive a value from it (a
 * property, a trip cost, a tag in
 * {@link uk.ac.ox.poseidon.agents.vessels.values.tags}). In a test relating a
 * vessel to another vessel, {@link uk.ac.ox.poseidon.agents.vessels.values.Other}
 * reads such a value off the other vessel. See
 * {@link uk.ac.ox.poseidon.agents.vessels.values.Factories} for the entry
 * points.
 */
package uk.ac.ox.poseidon.agents.vessels.values;
