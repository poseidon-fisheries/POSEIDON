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

import com.google.protobuf.util.Timestamps;
import eu.project.surimi.Workflow;
import uk.ac.ox.poseidon.core.Simulation;

import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneOffset;

import static java.lang.System.Logger.Level.INFO;

public class SimulateStepRequestHandler extends
    WithSimulationRequestHandler<Workflow.SimulateStepRequest, Workflow.SimulateStepResponse> {

    private static final System.Logger logger =
        System.getLogger(SimulateStepRequestHandler.class.getName());

    public SimulateStepRequestHandler(final SimulationManager simulationManager) {
        super(simulationManager);
    }

    @Override
    protected String getSimulationId(final Workflow.SimulateStepRequest request) {
        return request.getSimulationId();
    }

    @Override
    protected Workflow.SimulateStepResponse getResponseWithSimulation(
        final Workflow.SimulateStepRequest request,
        final Simulation simulation
    ) {
        final Period stepSize = simulationManager.getSimulationProperties(simulation).stepSize();
        simulation.getTemporalSchedule().stepFor(simulation, stepSize);
        final LocalDateTime dateTime = simulation.getTemporalSchedule().getDateTime();
        logger.log(
            INFO, "Advanced simulation {0} by {1} to {2}",
            request.getSimulationId(), stepSize, dateTime
        );
        return Workflow.SimulateStepResponse
            .newBuilder()
            .setDateTime(
                Timestamps.fromSeconds(dateTime
                    .toInstant(ZoneOffset.UTC)
                    .getEpochSecond())
            )
            .build();
    }
}
