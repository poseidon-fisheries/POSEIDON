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

package uk.ac.ox.poseidon.agents;

import lombok.Data;
import lombok.EqualsAndHashCode;
import uk.ac.ox.poseidon.core.scopes.SimulationScope;

import java.lang.ref.WeakReference;

/**
 * A {@link SimulationScope} narrowed to a single agent, for factories building agent-owned
 * components. Holds the agent via a {@link WeakReference} rather than directly, so that scoping to
 * an agent never itself keeps that agent alive past its natural lifetime.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AgentScope<G extends Agent> extends SimulationScope {

    private final WeakReference<G> agent;

    public AgentScope(final AgentScope<G> agentScope) {
        super(agentScope);
        this.agent = agentScope.agent;
    }

    public AgentScope(
        final SimulationScope simulationScope,
        final G agent
    ) {
        super(simulationScope);
        this.agent = new WeakReference<>(agent);
    }

    /** @return the scoped agent, or {@code null} if it has since been garbage-collected */
    public G getAgent() {
        return agent.get();
    }

}
