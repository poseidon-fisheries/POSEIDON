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
import lombok.RequiredArgsConstructor;
import org.apache.commons.beanutils.PropertyUtils;
import uk.ac.ox.poseidon.core.Scenario;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.utils.ConstantFactory;
import uk.ac.ox.poseidon.io.ScenarioLoader;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeParseException;
import java.util.UUID;

import static io.grpc.Status.*;
import static java.lang.System.Logger.Level.INFO;

@RequiredArgsConstructor
public class InitRequestHandler
    extends RequestHandler<Workflow.InitRequest, Workflow.InitResponse> {

    private static final System.Logger logger =
        System.getLogger(InitRequestHandler.class.getName());

    private final SimulationManager simulationManager;
    private final ScenarioLoader scenarioLoader;
    private final File scenarioFile;

    @Override
    protected Workflow.InitResponse getResponse(final Workflow.InitRequest request) {
        final UUID simulationId = parseId(request.getSimulationId());
        if (simulationManager.contains(simulationId)) {
            throw ALREADY_EXISTS
                .withDescription("Simulation already initialised: " + simulationId)
                .asRuntimeException();
        }
        final Scenario scenario = scenarioLoader.load(scenarioFile);
        final LocalDateTime startDateTime = toLocalDateTime(request.getStartDateTime());
        setScenarioProperty(
            scenario,
            "startingDateTime",
            new ConstantFactory<>(startDateTime)
        );
        logger.log(INFO, "Scenario loaded: {0}", scenarioFile);

        final Simulation simulation = scenario.newSimulation();
        final Period stepSize = parsePeriod(request.getStepSize());
        simulation.start();
        simulation.getTemporalSchedule().stepUntil(simulation, startDateTime);
        logger.log(
            INFO, "Simulation {0} started at {1}",
            simulationId, simulation.getTemporalSchedule().getDateTime()
        );
        simulationManager.put(
            simulationId,
            simulation,
            new SimulationManager.SimulationProperties(stepSize)
        );
        return Workflow.InitResponse.newBuilder().build();
    }

    @SuppressWarnings("SameParameterValue")
    private void setScenarioProperty(
        final Scenario scenario,
        final String propertyName,
        final Object value
    ) {
        try {
            PropertyUtils.setProperty(scenario, propertyName, value);
        } catch (
            final IllegalAccessException | InvocationTargetException | NoSuchMethodException e
        ) {
            throw FAILED_PRECONDITION
                .withDescription("Unable to set property " + propertyName + " to " + value)
                .withCause(e)
                .asRuntimeException();
        }
    }

    private Period parsePeriod(final String period) {
        try {
            return Period.parse(period);
        } catch (final DateTimeParseException e) {
            throw INVALID_ARGUMENT
                .withDescription("Invalid period: " + period)
                .withCause(e)
                .asRuntimeException();
        }
    }

}
