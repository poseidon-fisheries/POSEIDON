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

import io.grpc.Server;
import io.grpc.ServerBuilder;
import lombok.RequiredArgsConstructor;
import uk.ac.ox.poseidon.io.ScenarioLoader;

import java.io.IOException;
import java.lang.System.Logger;

import static java.lang.System.Logger.Level.INFO;

@RequiredArgsConstructor
public class AbmServer {

    private static final Logger logger = System.getLogger(AbmServer.class.getName());

    private final int port;
    private final Server grpcServer;

    public AbmServer(final int port) {
        this(
            port,
            ServerBuilder
                .forPort(port)
                .addService(new AbmService(new SimulationManager(new ScenarioLoader())))
                .build()
        );
    }

    public void start() throws IOException {
        grpcServer.start();
        logger.log(INFO, "Server started, listening on " + port);
    }

    public void shutdown() {
        grpcServer.shutdown();
    }

}
