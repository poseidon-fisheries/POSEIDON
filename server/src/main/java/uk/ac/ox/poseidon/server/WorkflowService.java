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
import com.google.protobuf.util.Timestamps;
import eu.project.surimi.Workflow;
import eu.project.surimi.WorkflowServiceGrpc;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import uk.ac.ox.poseidon.core.Simulation;

import java.io.File;
import java.time.Period;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static java.lang.System.Logger.Level.INFO;

@RequiredArgsConstructor
class WorkflowService extends WorkflowServiceGrpc.WorkflowServiceImplBase {

    private static final System.Logger logger = System.getLogger(WorkflowService.class.getName());
    private final SimulationManager simulationManager;
    private final File scenarioFile;

    private final Cache<Simulation, SimulationProperties> simulationProperties =
        CacheBuilder.newBuilder().weakKeys().build();

    @Override
    public void init(
        final Workflow.InitRequest request,
        final StreamObserver<Workflow.InitResponse> responseObserver
    ) {
        final UUID scenarioId = simulationManager.loadScenario(scenarioFile);
        simulationManager.setProperty(scenarioId, "startingDateTime", request.getStartDateTime());
        logger.log(INFO, "Scenario loaded: {0}", scenarioId);

        final UUID simulationId = simulationManager.startSimulation(scenarioId);
        logger.log(INFO, "Simulation started: {0}", simulationId);

        // TODO: handle period parsing errors
        simulationProperties.put(
            simulationManager.getSimulation(simulationId),
            new SimulationProperties(Period.parse(request.getStepSize()))
        );

        responseObserver.onNext(
            Workflow.InitResponse
                .newBuilder()
                .setSimulationId(simulationId.toString())
                .build()
        );
        responseObserver.onCompleted();
    }

    @Override
    public void simulateStep(
        final Workflow.SimulateStepRequest request,
        final StreamObserver<Workflow.SimulateStepResponse> responseObserver
    ) {
        final Simulation simulation = getSimulation(request.getSimulationId());
        final Period stepSize = Optional
            .ofNullable(simulationProperties.getIfPresent(simulation))
            .orElseThrow()
            .stepSize();
        simulation
            .getTemporalSchedule()
            .stepFor(simulation, stepSize);
        responseObserver.onNext(
            Workflow.SimulateStepResponse
                .newBuilder()
                .setDateTime(
                    Timestamps.fromSeconds(
                        simulation
                            .getTemporalSchedule()
                            .getDateTime()
                            .toInstant(ZoneOffset.UTC)
                            .getEpochSecond()
                    )
                )
                .build()
        );
        responseObserver.onCompleted();
    }

    private Simulation getSimulation(final String simulationId) {
        return simulationManager.getSimulation(UUID.fromString(simulationId));
    }

    @Override
    public void updatePrices(
        final Workflow.UpdatePricesRequest request,
        final StreamObserver<Workflow.UpdatePricesResponse> responseObserver
    ) {
        logger.log(INFO, "Received updatePrices request.");
/*
        // TODO: I'm currently looping through all the simulations in the simulation manager
        //  because we are not passing a simulation id in the request, but this is not a good
        //  long-term solution. Note that I'm loop through all simulations instead of just first
        //  or last one, but I prefer not to implement logic that legitimizes the current situation
        simulations
            .values()
            .stream()
            .map(simulationManager::getSimulation)
            .forEach(simulation -> {
                final Map<String, Species> speciesByCode =
                    simulation.getComponents(Species.class).stream()
                        .collect(toMap(Species::getCode, identity()));
                simulation
                    .getComponents(BiomassMarketGrid.class)
                    .forEach(marketGrid -> {
                        request.getPricesList().forEach(price -> {
                            marketGrid
                                .getObject(price.getPortId())
                                .ifPresent(market ->
                                    Optional
                                        .ofNullable(speciesByCode.get(price.getSpeciesId()))
                                        .ifPresent(
                                            species -> {
                                                final BiomassMarket.Price marketPrice =
                                                    new BiomassMarket.Price(
                                                        Money.of(
                                                            CurrencyUnit.of(price.getCurrency()),
                                                            price.getPrice(),
                                                            RoundingMode.HALF_EVEN
                                                        ),
                                                        BiomassMarket.parseMassUnit(price
                                                        .getMeasurementUnit())
                                                    );
                                                market.setPrice(species, marketPrice);
                                                logger.log(
                                                    INFO,
                                                    "Updated price of species {0} at port market " +
                                                        "{1} to {2}/{3}",
                                                    species.getCode(),
                                                    market.getId(),
                                                    marketPrice.amount(),
                                                    marketPrice.biomassUnit()
                                                );
                                            }
                                        )
                                );
                        });
                    });
            });
*/
        responseObserver.onNext(Workflow.UpdatePricesResponse.newBuilder().build());
        responseObserver.onCompleted();
    }

    private record SimulationProperties(Period stepSize) {}
}
