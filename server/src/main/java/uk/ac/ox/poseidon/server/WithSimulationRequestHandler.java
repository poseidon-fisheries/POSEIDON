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

import lombok.RequiredArgsConstructor;
import uk.ac.ox.poseidon.core.Simulation;

@RequiredArgsConstructor
public abstract class WithSimulationRequestHandler<ReqT, RespT>
    extends RequestHandler<ReqT, RespT> {

    final SimulationManager simulationManager;

    @SuppressWarnings("SynchronizeOnNonFinalField")
    @Override
    protected RespT getResponse(final ReqT request) {
        final Simulation simulation = simulationManager.getSimulation(getSimulationId(request));
        synchronized (simulation.schedule) {
            return getResponseWithSimulation(request, simulation);
        }
    }

    protected abstract String getSimulationId(final ReqT request);

    protected abstract RespT getResponseWithSimulation(
        final ReqT request,
        final Simulation simulation
    );

}
