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
import eu.project.surimi.WorkflowServiceGrpc;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class WorkflowService extends WorkflowServiceGrpc.WorkflowServiceImplBase {

    private final InitRequestHandler initRequestHandler;
    private final SimulateStepRequestHandler simulateStepRequestHandler;
    private final UpdatePricesRequestHandler updatePricesRequestHandler;
    private final RequestBiomassRequestHandler requestBiomassRequestHandler;
    private final UpdateBiomassRequestHandler updateBiomassRequestHandler;

    @Override
    public void init(
        final Workflow.InitRequest request,
        final StreamObserver<Workflow.InitResponse> responseObserver
    ) {
        initRequestHandler.handle(request, responseObserver);
    }

    @Override
    public void simulateStep(
        final Workflow.SimulateStepRequest request,
        final StreamObserver<Workflow.SimulateStepResponse> responseObserver
    ) {
        simulateStepRequestHandler.handle(request, responseObserver);
    }

    @Override
    public void updatePrices(
        final Workflow.UpdatePricesRequest request,
        final StreamObserver<Workflow.UpdatePricesResponse> responseObserver
    ) {
        updatePricesRequestHandler.handle(request, responseObserver);
    }

    @Override
    public void requestBiomass(
        final Workflow.RequestBiomassRequest request,
        final StreamObserver<Workflow.RequestBiomassResponse> responseObserver
    ) {
        requestBiomassRequestHandler.handle(request, responseObserver);
    }

    @Override
    public void updateBiomass(
        final Workflow.UpdateBiomassRequest request,
        final StreamObserver<Workflow.UpdateBiomassResponse> responseObserver
    ) {
        updateBiomassRequestHandler.handle(request, responseObserver);
    }

}
