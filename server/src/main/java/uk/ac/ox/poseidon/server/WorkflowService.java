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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.protobuf.Empty;
import eu.project.surimi.Surimi;
import eu.project.surimi.WorkflowGrpc;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.util.Optional;
import java.util.UUID;

import static java.lang.System.Logger.Level.INFO;

@RequiredArgsConstructor
class WorkflowService extends WorkflowGrpc.WorkflowImplBase {

    private static final System.Logger logger = System.getLogger(WorkflowService.class.getName());
    private final SimulationManager simulationManager;
    private final File scenarioFile;

    private final Cache<String, UUID> simulations = CacheBuilder.newBuilder().build();

    @Override
    public void init(
        final Surimi.InitRequest request,
        final StreamObserver<Empty> responseObserver
    ) {
        final String experimentId = request.getExperimentId();
        logger.log(INFO, "Received init request for experiment: {0}", experimentId);
        Optional
            .ofNullable(simulations.getIfPresent(experimentId))
            .ifPresentOrElse(
                simulationId ->
                    logger.log(INFO, "Simulation already started: {0}", simulationId),
                () -> {
                    final UUID scenarioId = simulationManager.loadScenario(scenarioFile);
                    logger.log(INFO, "Scenario loaded: {0}", scenarioId);
                    final UUID simulationId = simulationManager.startSimulation(scenarioId);
                    simulations.put(experimentId, simulationId);
                    logger.log(INFO, "Simulation started: {0}", simulationId);
                }
            );
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }
}
