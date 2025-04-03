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
import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Timestamps;
import eu.project.surimi.Workflow;
import eu.project.surimi.WorkflowServiceGrpc;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.apache.commons.beanutils.PropertyUtils;
import org.joda.money.CurrencyUnit;
import org.joda.money.IllegalCurrencyException;
import org.joda.money.Money;
import uk.ac.ox.poseidon.agents.market.BiomassMarket;
import uk.ac.ox.poseidon.agents.market.BiomassMarketGrid;
import uk.ac.ox.poseidon.biology.species.Species;
import uk.ac.ox.poseidon.core.Scenario;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.utils.ConstantFactory;
import uk.ac.ox.poseidon.geography.grids.ObjectGrid;
import uk.ac.ox.poseidon.io.ScenarioLoader;

import javax.measure.Unit;
import javax.measure.format.MeasurementParseException;
import javax.measure.quantity.Mass;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static io.grpc.Status.*;
import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;
import static java.util.function.UnaryOperator.identity;
import static java.util.stream.Collectors.toMap;

@RequiredArgsConstructor
class WorkflowService extends WorkflowServiceGrpc.WorkflowServiceImplBase {

    private static final System.Logger logger = System.getLogger(WorkflowService.class.getName());
    private final Cache<UUID, Simulation> simulations = CacheBuilder.newBuilder().build();
    private final ScenarioLoader scenarioLoader;
    private final File scenarioFile;

    private final Cache<Simulation, SimulationProperties> simulationProperties =
        CacheBuilder.newBuilder().weakKeys().build();

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

    private static <ReqT, RespT> void safeUnaryCall(
        final ReqT request,
        final StreamObserver<RespT> responseObserver,
        final UnaryHandler<ReqT, RespT> handler
    ) {
        try {
            logger.log(INFO, "Handling request:\n{0}", request);
            final RespT response = handler.handle(request);
            responseObserver.onNext(response);
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

    private static LocalDateTime toLocalDateTime(final Timestamp startDateTime) {
        return LocalDateTime.ofInstant(
            Instant.ofEpochSecond(
                startDateTime.getSeconds()), ZoneOffset.UTC
        );
    }

    @Override
    public void init(
        final Workflow.InitRequest request,
        final StreamObserver<Workflow.InitResponse> responseObserver
    ) {
        safeUnaryCall(
            request, responseObserver, req -> {
                final UUID simulationId = parseId(request.getSimulationId());
                if (simulations.asMap().containsKey(simulationId)) {
                    throw ALREADY_EXISTS
                        .withDescription("Simulation already initialised: " + simulationId)
                        .asRuntimeException();
                }
                final Scenario scenario = scenarioLoader.load(scenarioFile);
                final LocalDateTime startDateTime = toLocalDateTime(request.getStartDateTime());
                setScenarioProperty(
                    scenario,
                    "startingDateTime",
                    new ConstantFactory<>(startDateTime)
                );
                logger.log(INFO, "Scenario loaded: {0}", scenarioFile);

                final Simulation simulation = scenario.newSimulation();
                simulationProperties.put(
                    simulation,
                    new SimulationProperties(parsePeriod(request.getStepSize()))
                );
                simulation.start();
                simulation.getTemporalSchedule().stepUntil(simulation, startDateTime);
                logger.log(
                    INFO, "Simulation {0} started at {1}",
                    simulationId, simulation.getTemporalSchedule().getDateTime()
                );
                simulations.put(simulationId, simulation);

                return Workflow.InitResponse.newBuilder().build();
            }
        );
    }

    private Period parsePeriod(final String period) {
        try {
            return Period.parse(period);
        } catch (final DateTimeParseException e) {
            throw INVALID_ARGUMENT
                .withDescription("Invalid period: " + period)
                .withCause(e)
                .asRuntimeException();
        }
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

    @Override
    public void simulateStep(
        final Workflow.SimulateStepRequest request,
        final StreamObserver<Workflow.SimulateStepResponse> responseObserver
    ) {
        safeUnaryCall(
            request, responseObserver, req -> {
                final Simulation simulation = getSimulation(request.getSimulationId());
                final Period stepSize = getSimulationProperties(simulation).stepSize();
                simulation.getTemporalSchedule().stepFor(simulation, stepSize);
                final LocalDateTime dateTime = simulation.getTemporalSchedule().getDateTime();
                logger.log(
                    INFO, "Advanced simulation {0} by {1} to {2}",
                    request.getSimulationId(), stepSize, dateTime
                );
                return
                    Workflow.SimulateStepResponse
                        .newBuilder()
                        .setDateTime(
                            Timestamps.fromSeconds(dateTime
                                .toInstant(ZoneOffset.UTC)
                                .getEpochSecond())
                        )
                        .build();
            }
        );
    }

    private SimulationProperties getSimulationProperties(final Simulation simulation) {
        final SimulationProperties properties = simulationProperties.getIfPresent(simulation);
        if (properties == null) throw INTERNAL
            .withDescription("Unable to get simulation properties.")
            .asRuntimeException();
        return properties;
    }

    @Override
    public void updatePrices(
        final Workflow.UpdatePricesRequest request,
        final StreamObserver<Workflow.UpdatePricesResponse> responseObserver
    ) {
        safeUnaryCall(
            request, responseObserver, req -> {
                final Simulation simulation = getSimulation(request.getSimulationId());
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
        );
    }

    @SuppressWarnings("SameParameterValue")
    private void setScenarioProperty(
        final Scenario scenario,
        final String propertyName,
        final Object value
    ) {
        try {
            PropertyUtils.setProperty(scenario, propertyName, value);
        } catch (
            final IllegalAccessException | InvocationTargetException | NoSuchMethodException e
        ) {
            throw FAILED_PRECONDITION
                .withDescription("Unable to set property " + propertyName + " to " + value)
                .withCause(e)
                .asRuntimeException();
        }
    }

    @Override
    public void requestBiomass(
        final Workflow.RequestBiomassRequest request,
        final StreamObserver<Workflow.RequestBiomassResponse> responseObserver
    ) {
        super.requestBiomass(request, responseObserver);
    }

    @Override
    public void updateBiomass(
        final Workflow.UpdateBiomassRequest request,
        final StreamObserver<Workflow.UpdateBiomassResponse> responseObserver
    ) {
        super.updateBiomass(request, responseObserver);
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

    @FunctionalInterface
    interface UnaryHandler<ReqT, RespT> {
        RespT handle(ReqT request) throws Exception;
    }

    private record SimulationProperties(Period stepSize) {}
}
