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

package uk.ac.ox.poseidon.agents.tasks;

import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import uk.ac.ox.poseidon.agents.Agent;

/**
 * A gdx-ai {@link LeafTask} run against an {@link Agent}. Behavior trees are built fresh per
 * agent rather than cloned, so {@link #copyTo} is deliberately unsupported.
 *
 * @param <G> the type of agent this task runs against
 */
public abstract class AgentTask<G extends Agent> extends LeafTask<G> {

    /** @return the agent this task is running against */
    public G getAgent() {
        return getObject();
    }

    /** @throws UnsupportedOperationException always; behavior trees are never cloned */
    @Override
    protected Task<G> copyTo(final Task<G> task) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
