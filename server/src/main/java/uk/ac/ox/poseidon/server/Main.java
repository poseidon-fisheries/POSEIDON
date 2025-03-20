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
import io.grpc.Server;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import uk.ac.ox.poseidon.core.utils.CustomPathConverter;
import uk.ac.ox.poseidon.io.ScenarioLoader;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.Arrays;

import static java.lang.System.Logger.Level.INFO;

public class Main {

    private static final System.Logger logger = System.getLogger(Main.class.getName());

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
        final Main main = new Main();
        final JCommander jCommander = JCommander
            .newBuilder()
            .addObject(main)
            .build();
        try {
            jCommander.parse(args);
            main.startServer();
        } catch (final ParameterException | IOException | InterruptedException e) {
            System.err.println(e.getMessage());
        }
    }

    private void startServer() throws IOException, InterruptedException {
        final SimulationManager simulationManager = new SimulationManager(new ScenarioLoader());
        final WorkflowService workflowService = new WorkflowService(
            simulationManager,
            scenarioPath.toFile()
        );
        // Bind to 0.0.0.0 so the server listens on all network interfaces
        final Server grpcServer = NettyServerBuilder
            .forAddress(new InetSocketAddress("0.0.0.0", this.port))
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

}
