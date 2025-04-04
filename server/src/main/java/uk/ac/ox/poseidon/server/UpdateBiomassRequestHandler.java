/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025 CoHESyS Lab cohesys.lab@gmail.com
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
 *
 */

package uk.ac.ox.poseidon.server;

import eu.project.surimi.Workflow;
import uk.ac.ox.poseidon.core.Simulation;

public class UpdateBiomassRequestHandler extends
    WithSimulationRequestHandler<Workflow.UpdateBiomassRequest, Workflow.UpdateBiomassResponse> {

    public UpdateBiomassRequestHandler(final SimulationManager simulationManager) {
        super(simulationManager);
    }

    @Override
    protected String getSimulationId(final Workflow.UpdateBiomassRequest request) {
        return request.getSimulationId();
    }

    @Override
    protected Workflow.UpdateBiomassResponse getResponseWithSimulation(
        final Workflow.UpdateBiomassRequest request,
        final Simulation simulation
    ) {
        // TODO: not implemented yet
        return Workflow.UpdateBiomassResponse.newBuilder().build();
    }
}
