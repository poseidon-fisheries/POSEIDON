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

import com.google.protobuf.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.nio.file.Path;
import java.util.UUID;

import static java.lang.System.Logger.Level.INFO;

@RequiredArgsConstructor
public class AbmService extends AbmGrpc.AbmImplBase {

    private static final System.Logger logger = System.getLogger(AbmService.class.getName());
    private final SimulationManager simulationManager;

    @Override
    public void loadScenario(
        final AbmOuterClass.FilePath request,
        final StreamObserver<AbmOuterClass.ScenarioId> responseObserver
    ) {
        final File scenarioFile = Path.of(request.getPath()).toFile();
        logger.log(INFO, "Loading scenario: {0}", scenarioFile.getAbsolutePath());
        final UUID scenarioId = simulationManager.loadScenario(scenarioFile);
        logger.log(INFO, "Scenario loaded: {0}", scenarioId);
        responseObserver.onNext(
            AbmOuterClass.ScenarioId
                .newBuilder()
                .setId(scenarioId.toString())
                .build()
        );
        responseObserver.onCompleted();
    }

    @Override
    public void unloadScenario(
        final AbmOuterClass.ScenarioId request,
        final StreamObserver<Empty> responseObserver
    ) {
        simulationManager.unloadScenario(UUID.fromString(request.getId()));
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void startSimulation(
        final AbmOuterClass.ScenarioId request,
        final StreamObserver<AbmOuterClass.SimulationId> responseObserver
    ) {
        final UUID scenarioId = UUID.fromString(request.getId());
        final UUID simulationId = simulationManager.startSimulation(scenarioId);
        logger.log(INFO, "Simulation started: {0}", scenarioId);
        responseObserver.onNext(
            AbmOuterClass.SimulationId
                .newBuilder()
                .setId(simulationId.toString())
                .build()
        );
        responseObserver.onCompleted();
    }

    @Override
    public void finishSimulation(
        final AbmOuterClass.SimulationId request,
        final StreamObserver<Empty> responseObserver
    ) {
        final UUID simulationId = UUID.fromString(request.getId());
        simulationManager.finishSimulation(simulationId);
        logger.log(INFO, "Simulation finished: {0}", simulationId);
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void setProperty(
        final AbmOuterClass.SetPropertyRequest request,
        final StreamObserver<Empty> responseObserver
    ) {
        final UUID scenarioId;
        try {
            scenarioId = UUID.fromString(request.getScenarioId().getId());
        } catch (final IllegalArgumentException e) {
            responseObserver.onError(
                Status.INVALID_ARGUMENT
                    .withDescription("Invalid scenario id: " + request.getScenarioId())
                    .withCause(e)
                    .asRuntimeException()
            );
            return;
        }
        final String propertyName = request.getPropertyName();
        final Any anyValue = request.getValue();

        final Object value;
        try {
            value = extractValue(anyValue);
        } catch (final Exception e) {
            responseObserver.onError(
                Status.INVALID_ARGUMENT
                    .withDescription("Failed to unpack value: " + e.getMessage())
                    .withCause(e)
                    .asRuntimeException()
            );
            return;
        }

        try {
            simulationManager.setProperty(scenarioId, propertyName, value);
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (final SimulationManager.SetPropertyException e) {
            responseObserver.onError(
                Status.INTERNAL
                    .withDescription(e.getMessage())
                    .withCause(e)
                    .asRuntimeException()
            );
        }
    }

    private Object extractValue(final Any anyValue) throws Exception {
        if (anyValue.is(StringValue.class)) {
            return anyValue.unpack(StringValue.class).getValue();
        } else if (anyValue.is(Int32Value.class)) {
            return anyValue.unpack(Int32Value.class).getValue();
        } else if (anyValue.is(BoolValue.class)) {
            return anyValue.unpack(BoolValue.class).getValue();
        } else if (anyValue.is(DoubleValue.class)) {
            return anyValue.unpack(DoubleValue.class).getValue();
        }
        throw new IllegalArgumentException("Unsupported type: " + anyValue.getTypeUrl());
    }
}
