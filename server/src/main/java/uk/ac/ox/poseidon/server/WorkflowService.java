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
import eu.project.surimi.Biomass;
import eu.project.surimi.Workflow;
import eu.project.surimi.WorkflowServiceGrpc;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.joda.money.CurrencyUnit;
import org.joda.money.IllegalCurrencyException;
import org.joda.money.Money;
import uk.ac.ox.poseidon.agents.market.BiomassMarket;
import uk.ac.ox.poseidon.agents.market.BiomassMarketGrid;
import uk.ac.ox.poseidon.biology.biomass.BiomassGrid;
import uk.ac.ox.poseidon.biology.species.Species;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.geography.Coordinate;
import uk.ac.ox.poseidon.geography.bathymetry.BathymetricGrid;
import uk.ac.ox.poseidon.geography.grids.ObjectGrid;

import javax.measure.Unit;
import javax.measure.format.MeasurementParseException;
import javax.measure.quantity.Mass;
import java.math.RoundingMode;
import java.time.Period;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

import static io.grpc.Status.*;
import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;
import static java.util.function.UnaryOperator.identity;
import static java.util.stream.Collectors.toMap;
import static tech.units.indriya.unit.Units.KILOGRAM;

@RequiredArgsConstructor
class WorkflowService extends WorkflowServiceGrpc.WorkflowServiceImplBase {

    private static final System.Logger logger = System.getLogger(WorkflowService.class.getName());
    private final Cache<UUID, Simulation> simulations = CacheBuilder.newBuilder().build();

    private final InitRequestHandler initRequestHandler;
    private final SimulateStepRequestHandler simulateStepRequestHandler;

    private static Map<String, Species> getSpeciesByCode(final Simulation simulation) {
        return simulation
            .getComponents(Species.class)
            .stream()
            .collect(toMap(Species::getCode, identity()));
    }

    private static Set<BiomassMarketGrid> getBiomassMarketGrids(final Simulation simulation) {
        final Set<BiomassMarketGrid> marketGrids =
            simulation.getComponents(BiomassMarketGrid.class);
        if (marketGrids.isEmpty()) {
            throw FAILED_PRECONDITION
                .withDescription("No market grids defined in simulation.")
                .asRuntimeException();
        }
        return marketGrids;
    }

    private static UUID parseId(final String id) {
        try {
            return UUID.fromString(id);
        } catch (final IllegalArgumentException e) {
            throw INVALID_ARGUMENT
                .withDescription("Invalid UUID: " + id)
                .asRuntimeException();
        }
    }

    private static <K, V> V getOrThrow(
        final Map<K, V> map,
        final K key,
        final String name
    ) {
        final V v = map.get(key);
        if (v == null) {
            throw NOT_FOUND
                .withDescription(name + " not found: " + key)
                .asRuntimeException();
        }
        return v;
    }

    private static <RespT> void handle(
        final StreamObserver<RespT> responseObserver,
        final Supplier<RespT> responseSupplier
    ) {
        try {
            responseObserver.onNext(responseSupplier.get());
            responseObserver.onCompleted();
        } catch (final StatusRuntimeException e) {
            logger.log(ERROR, e);
            responseObserver.onError(e);
        } catch (final Exception e) {
            logger.log(ERROR, e);
            responseObserver.onError(
                Status.INTERNAL
                    .withDescription("Unexpected server error: " + e.getMessage())
                    .withCause(e)
                    .asRuntimeException()
            );
        }
    }

    @Override
    public void init(
        final Workflow.InitRequest request,
        final StreamObserver<Workflow.InitResponse> responseObserver
    ) {
        initRequestHandler.handle(request, responseObserver);
    }

    @SuppressWarnings("SameParameterValue")
    private StatusRuntimeException wrap(
        final Status status,
        final Exception e
    ) {
        return status
            .withDescription(e.getMessage())
            .withCause(e)
            .asRuntimeException();
    }

    private Simulation getSimulation(final String simulationId) {
        final Simulation simulation = simulations.getIfPresent(parseId(simulationId));
        if (simulation == null) {
            throw NOT_FOUND
                .withDescription("Simulation not found: " + simulationId)
                .asRuntimeException();
        }
        return simulation;
    }

    @SuppressWarnings("SynchronizeOnNonFinalField")
    private <RespT> Supplier<RespT> withSimulation(
        final String simulationId,
        final Function<Simulation, RespT> handler
    ) {
        final Simulation simulation = getSimulation(simulationId);
        return () -> {
            synchronized (simulation.schedule) {
                return handler.apply(simulation);
            }
        };
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
        handle(
            responseObserver,
            withSimulation(
                request.getSimulationId(),
                simulation -> {
                    final Map<String, Species> speciesByCode = getSpeciesByCode(simulation);
                    final Map<String, BiomassMarket> marketsById =
                        getBiomassMarketGrids(simulation)
                            .stream()
                            .flatMap(ObjectGrid::stream)
                            .collect(toMap(BiomassMarket::getId, identity()));
                    request.getPricesList().forEach(price -> {
                        final BiomassMarket market = getOrThrow(
                            marketsById,
                            price.getPortId(),
                            "Market"
                        );
                        final Species species = getOrThrow(
                            speciesByCode,
                            price.getSpeciesId(),
                            "Species"
                        );
                        final CurrencyUnit currencyUnit = parseCurrency(price.getCurrency());
                        final Unit<Mass> biomassUnit = parseMassUnit(price.getMeasurementUnit());
                        final BiomassMarket.Price marketPrice =
                            new BiomassMarket.Price(
                                Money.of(currencyUnit, price.getPrice(), RoundingMode.HALF_EVEN),
                                biomassUnit
                            );
                        market.setPrice(species, marketPrice);
                        logger.log(
                            INFO,
                            "Updated price of species {0} at port market {1} to {2}/{3}.",
                            species.getCode(),
                            market.getId(),
                            marketPrice.amount(),
                            marketPrice.biomassUnit()
                        );
                    });
                    return Workflow.UpdatePricesResponse.newBuilder().build();
                }
            )
        );
    }

    private <T> T getComponent(
        final Simulation simulation,
        final Class<T> componentClass
    ) {
        final Set<T> components = simulation.getComponents(componentClass);
        if (components.isEmpty()) {
            throw NOT_FOUND
                .withDescription("Component of class " +
                    componentClass.getName() +
                    " not found in simulation.")
                .asRuntimeException();
        }
        if (components.size() > 1) {
            throw FAILED_PRECONDITION
                .withDescription("More than one component of class " +
                    componentClass.getName() +
                    " found in simulation.")
                .asRuntimeException();
        }
        return components.iterator().next();
    }

    @Override
    public void requestBiomass(
        final Workflow.RequestBiomassRequest request,
        final StreamObserver<Workflow.RequestBiomassResponse> responseObserver
    ) {
        handle(
            responseObserver,
            withSimulation(
                request.getSimulationId(),
                simulation -> {
                    final BathymetricGrid bathymetricGrid =
                        getComponent(simulation, BathymetricGrid.class);
                    final Workflow.RequestBiomassResponse.Builder responseBuilder =
                        Workflow.RequestBiomassResponse
                            .newBuilder()
                            .setMeasurementUnit(KILOGRAM.getSymbol());
                    simulation.getComponents(BiomassGrid.class).forEach(grid -> {
                        final Biomass.BiomassGrid.Builder gridBuilder =
                            Biomass.BiomassGrid
                                .newBuilder()
                                .setSpeciesId(grid.getSpecies().getCode());
                        bathymetricGrid.getActiveWaterCells().forEach(cell -> {
                            final Coordinate coordinate =
                                bathymetricGrid.getModelGrid().toCoordinate(cell);
                            gridBuilder.addBiomassCells(
                                Biomass.BiomassCell
                                    .newBuilder()
                                    .setLongitude(coordinate.getLon())
                                    .setLatitude(coordinate.getLat())
                                    .setBiomass(grid.getDouble(cell))
                                    .build()
                            );
                        });
                        responseBuilder.addBiomassGrids(gridBuilder.build());
                    });
                    return responseBuilder.build();
                }
            )
        );
    }

    @Override
    public void updateBiomass(
        final Workflow.UpdateBiomassRequest request,
        final StreamObserver<Workflow.UpdateBiomassResponse> responseObserver
    ) {
        handle(
            responseObserver,
            withSimulation(
                request.getSimulationId(),
                simulation -> Workflow.UpdateBiomassResponse
                    .newBuilder()
                    .build()
            )
        );
    }

    private CurrencyUnit parseCurrency(final String currency) {
        try {
            return CurrencyUnit.of(currency);
        } catch (final IllegalCurrencyException e) {
            throw wrap(INVALID_ARGUMENT, e);
        }
    }

    private Unit<Mass> parseMassUnit(final String massUnit) {
        try {
            return BiomassMarket.parseMassUnit(massUnit);
        } catch (final MeasurementParseException e) {
            throw wrap(INVALID_ARGUMENT, e);
        }
    }

    private record SimulationProperties(Period stepSize) {}
}
