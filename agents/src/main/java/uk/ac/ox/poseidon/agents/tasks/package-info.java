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

/**
 * The base machinery for an agent's gdx-ai behavior tree:
 * {@link uk.ac.ox.poseidon.agents.tasks.AgentTask}/
 * {@link uk.ac.ox.poseidon.agents.tasks.ExtendedTask} for building leaf tasks,
 * {@link uk.ac.ox.poseidon.agents.tasks.TaskFactory}/
 * {@link uk.ac.ox.poseidon.agents.tasks.VesselTaskFactory} for their factories, and
 * {@link uk.ac.ox.poseidon.agents.tasks.Behaviour} for wrapping a whole tree (or none) as the
 * agent's top-level behavior. Concrete leaves and branch/decorator nodes live in sibling
 * packages ({@link uk.ac.ox.poseidon.agents.tasks.branches},
 * {@link uk.ac.ox.poseidon.agents.tasks.decorators},
 * {@link uk.ac.ox.poseidon.agents.tasks.general}, {@link uk.ac.ox.poseidon.agents.tasks.fishing},
 * {@link uk.ac.ox.poseidon.agents.tasks.travel}). See
 * {@link uk.ac.ox.poseidon.agents.tasks.Factories} for this package's own entry points.
 */
package uk.ac.ox.poseidon.agents.tasks;
