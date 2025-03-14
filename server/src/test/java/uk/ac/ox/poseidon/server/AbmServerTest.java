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

import com.google.protobuf.Any;
import com.google.protobuf.StringValue;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class AbmServerTest {

    private static final String HOST = "localhost";
    private static final int PORT = 8080;

    @Test
    void testServer() {

        final AbmServer server = new AbmServer(PORT);
        assertDoesNotThrow(server::start);

        final ManagedChannel channel = ManagedChannelBuilder.forAddress(HOST, PORT)
            .usePlaintext()
            .build();

        final AbmGrpc.AbmBlockingStub stub = AbmGrpc.newBlockingStub(channel);

        final AbmOuterClass.FilePath scenarioFilePath =
            AbmOuterClass.FilePath
                .newBuilder()
                .setPath("/home/nicolas/workspace/surimi_western_med/scenario.yaml")
                .build();

        final AbmOuterClass.ScenarioId scenarioId = stub.loadScenario(scenarioFilePath);

        stub.setProperty(
            AbmOuterClass.SetPropertyRequest
                .newBuilder()
                .setScenarioId(scenarioId)
                .setPropertyName("inputPath.path")
                .setValue(
                    Any.pack(
                        StringValue
                            .newBuilder()
                            .setValue("/home/nicolas/workspace/surimi_western_med/data")
                            .build()
                    )
                )
                .build()
        );

        final AbmOuterClass.SimulationId simulationId = stub.startSimulation(scenarioId);

        channel.shutdown();
        server.shutdown();

    }

}
