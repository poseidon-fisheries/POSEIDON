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

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionServiceV1;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.grpc.v1_6.GrpcTelemetry;
import uk.ac.ox.poseidon.core.utils.CustomPathConverter;
import uk.ac.ox.poseidon.io.ScenarioLoader;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.Arrays;

import static java.lang.System.Logger.Level.INFO;

public class Server {

    private static final System.Logger logger = System.getLogger(Server.class.getName());

    @Parameter(
        names = {"-s", "--scenario"},
        description = "Path to the scenario file in YAML format.",
        converter = CustomPathConverter.class,
        required = true
    )
    private Path scenarioPath;

    @Parameter(
        names = {"-p", "--port"},
        description = "Port to listen on for gRPC requests.",
        required = true
    )
    private int port;

    public static void main(final String[] args) {
        logger.log(INFO, () -> "Received arguments: " + Arrays.toString(args));
        final Server server = new Server();
        final JCommander jCommander = JCommander
            .newBuilder()
            .addObject(server)
            .build();
        try {
            jCommander.parse(args);
            server.startServer();
        } catch (final ParameterException | IOException | InterruptedException e) {
            System.err.println(e.getMessage());
        }
    }

    private void startServer() throws IOException, InterruptedException {
        final OpenTelemetry openTelemetry = OpenTelemetryConfiguration.initOpenTelemetry();
        final WorkflowService workflowService = createWorkflowService();
        // Bind to 0.0.0.0 so the server listens on all network interfaces
        final io.grpc.Server grpcServer = NettyServerBuilder
            .forAddress(new InetSocketAddress("0.0.0.0", this.port))
            .addService(ProtoReflectionServiceV1.newInstance())
            .intercept(GrpcTelemetry.create(openTelemetry).newServerInterceptor())
            .addService(workflowService)
            .build();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.log(INFO, "Shutting down gRPC server...");
            grpcServer.shutdown();
            logger.log(INFO, "Server shut down.");
        }));
        grpcServer.start();
        logger.log(INFO, "Server started, listening on " + port);
        grpcServer.awaitTermination();
    }

    private WorkflowService createWorkflowService() {
        final SimulationManager simulationManager = new SimulationManager();
        return new WorkflowService(
            new InitRequestHandler(
                simulationManager,
                new ScenarioLoader(),
                scenarioPath.toFile()
            ),
            new SimulateStepRequestHandler(simulationManager),
            new UpdatePricesRequestHandler(simulationManager),
            new RequestBiomassRequestHandler(simulationManager),
            new UpdateBiomassRequestHandler(simulationManager)
        );
    }

}
