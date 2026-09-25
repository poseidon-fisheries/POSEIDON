/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024-2026, University of Oxford.
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
 * {@link uk.ac.ox.poseidon.agents.vessels.Vessel}, the core fishing agent, and
 * {@link uk.ac.ox.poseidon.agents.vessels.Fleet}, the set of vessels operating out of a set of
 * ports. {@link uk.ac.ox.poseidon.agents.vessels.VesselScope} narrows factory resolution to a
 * single vessel. Vessels are created and updated via
 * {@link uk.ac.ox.poseidon.agents.vessels.FleetEvent}s, either scripted directly
 * ({@link uk.ac.ox.poseidon.agents.vessels.VesselActivationFactory},
 * {@link uk.ac.ox.poseidon.agents.vessels.VesselCreatorFactory}) or read from a vessel register
 * table ({@link uk.ac.ox.poseidon.agents.vessels.FleetFromVesselRegisterFactory}). A vessel's
 * hold/gear/engine/behaviour/etc. live in sibling packages
 * ({@link uk.ac.ox.poseidon.agents.vessels.holds},
 * {@link uk.ac.ox.poseidon.agents.vessels.gears},
 * {@link uk.ac.ox.poseidon.agents.vessels.engines},
 * {@link uk.ac.ox.poseidon.agents.vessels.accounts},
 * {@link uk.ac.ox.poseidon.agents.vessels.predicates},
 * {@link uk.ac.ox.poseidon.agents.vessels.providers},
 * {@link uk.ac.ox.poseidon.agents.vessels.extractors},
 * {@link uk.ac.ox.poseidon.agents.vessels.friends}). See
 * {@link uk.ac.ox.poseidon.agents.vessels.Factories} for this package's own entry points.
 */
package uk.ac.ox.poseidon.agents.vessels;
