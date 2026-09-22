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
 * A lightweight publish/subscribe event system ({@link uk.ac.ox.poseidon.core.events.EventManager},
 * {@link uk.ac.ox.poseidon.core.events.Listener}) used to decouple event producers from consumers
 * across the simulation, plus listener implementations that accumulate or fold received events.
 * Most types here are plain classes constructed directly, not via the Factory pattern; see
 * {@link uk.ac.ox.poseidon.core.events.Factories} for the one component that is.
 */
package uk.ac.ox.poseidon.core.events;
