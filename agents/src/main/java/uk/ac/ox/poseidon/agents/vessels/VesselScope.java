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

package uk.ac.ox.poseidon.agents.vessels;

import lombok.Data;
import lombok.EqualsAndHashCode;
import uk.ac.ox.poseidon.agents.AgentScope;
import uk.ac.ox.poseidon.core.scopes.SimulationScope;

/** An {@link AgentScope} narrowed to a {@link Vessel}, for factories building vessel-owned components. */
@Data
@EqualsAndHashCode(callSuper = true)
public class VesselScope extends AgentScope<Vessel> {

    /** @param vesselScope the scope to copy */
    public VesselScope(final VesselScope vesselScope) {
        super(vesselScope);
    }

    /**
     * @param simulationScope the simulation scope to narrow
     * @param vessel          the vessel to scope to
     */
    public VesselScope(
        final SimulationScope simulationScope,
        final Vessel vessel
    ) {
        super(new AgentScope<>(simulationScope, vessel));
    }

    /** @return the scoped vessel, or {@code null} if it has since been garbage-collected */
    public Vessel getVessel() {
        return getAgent();
    }

}
