/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2026, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
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
 */

package uk.ac.ox.poseidon.examples.petersnapper;

import sim.util.Int2D;
import static uk.ac.ox.poseidon.agents.catches.Factories.uncategorisedCatchCategory;
import static uk.ac.ox.poseidon.agents.catches.Factories.uniformCatchCategoriser;
import uk.ac.ox.poseidon.agents.catches.disposition.ProportionallyLimitingBiomassToHoldFactory;
import uk.ac.ox.poseidon.agents.choices.*;
import uk.ac.ox.poseidon.agents.components.ComponentRegisterFactory;
import static uk.ac.ox.poseidon.agents.fields.Factories.vesselField;
import static uk.ac.ox.poseidon.agents.fisheables.Factories.currentCellFisheable;
import static uk.ac.ox.poseidon.agents.fuel.Factories.fuelStationGrid;
import static uk.ac.ox.poseidon.agents.fuel.Factories.oneFuelStationPerPort;
import uk.ac.ox.poseidon.agents.market.MarketGridFactory;
import uk.ac.ox.poseidon.agents.market.OneBiomassMarketPerPortFactory;
import uk.ac.ox.poseidon.agents.market.PriceEntryFactory;
import uk.ac.ox.poseidon.agents.market.PriceFactory;
import uk.ac.ox.poseidon.agents.tasks.BehaviourFactory;
import static uk.ac.ox.poseidon.agents.tasks.destinations.Factories.startTrip;
import uk.ac.ox.poseidon.agents.tasks.fishing.FishingFactory;
import uk.ac.ox.poseidon.agents.tasks.landings.LandCatchesFactory;
import uk.ac.ox.poseidon.agents.tasks.travel.EndTripFactory;
import uk.ac.ox.poseidon.agents.tasks.travel.SetDestinationToOriginFactory;
import uk.ac.ox.poseidon.agents.tasks.travel.TravelAlongPathFactory;
import uk.ac.ox.poseidon.agents.vessels.PrefixedIdFactory;
import uk.ac.ox.poseidon.agents.vessels.VesselCreatorFactory;
import uk.ac.ox.poseidon.agents.vessels.engines.SimpleEngineFactory;
import uk.ac.ox.poseidon.agents.vessels.gears.FixedBiomassProportionGearFactory;
import static uk.ac.ox.poseidon.agents.vessels.holds.Factories.standardBiomassHold;
import static uk.ac.ox.poseidon.biology.allocators.Factories.proportionOfCarryingCapacityAllocator;
import uk.ac.ox.poseidon.biology.biomass.*;
import uk.ac.ox.poseidon.biology.species.SpeciesFactory;
import uk.ac.ox.poseidon.core.Scenario;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.schedule.SteppableSequenceFactory;
import uk.ac.ox.poseidon.core.schedule.TemporalSchedule;
import uk.ac.ox.poseidon.core.utils.PairFactory;
import uk.ac.ox.poseidon.core.utils.PrefixedIdSupplierFactory;
import uk.ac.ox.poseidon.geography.CoordinateFactory;
import static uk.ac.ox.poseidon.geography.allocators.Factories.filteredAllocator;
import static uk.ac.ox.poseidon.geography.allocators.Factories.supplierAllocator;
import uk.ac.ox.poseidon.geography.bathymetry.BathymetricGridFromElevationTableFactory;
import static uk.ac.ox.poseidon.geography.distance.Factories.haversineDistanceCalculator;
import uk.ac.ox.poseidon.geography.grids.DoubleGrid;
import uk.ac.ox.poseidon.geography.grids.ModelGridFromLonLatTableFactory;
import uk.ac.ox.poseidon.geography.grids.NormalisedDoubleGridFromAllocatorFactory;
import uk.ac.ox.poseidon.geography.ports.PortFactory;
import uk.ac.ox.poseidon.geography.ports.PortGridFactory;
import uk.ac.ox.poseidon.io.ScenarioWriter;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.function.Supplier;

import static tech.units.indriya.unit.Units.*;
import static uk.ac.ox.poseidon.agents.choices.evaluation.Factories.profitPerHour;
import static uk.ac.ox.poseidon.agents.choices.evaluation.Factories.tripEvaluator;
import static uk.ac.ox.poseidon.agents.components.Factories.component;
import static uk.ac.ox.poseidon.agents.money.Factories.money;
import static uk.ac.ox.poseidon.agents.tasks.branches.Factories.sequenceTask;
import static uk.ac.ox.poseidon.agents.tasks.decorators.Factories.untilFail;
import static uk.ac.ox.poseidon.agents.tasks.general.Factories.checkThat;
import static uk.ac.ox.poseidon.agents.tasks.general.Factories.waitFor;
import static uk.ac.ox.poseidon.agents.tasks.travel.Factories.refuel;
import static uk.ac.ox.poseidon.agents.vessels.accounts.Factories.account;
import static uk.ac.ox.poseidon.agents.vessels.engines.Factories.fullTank;
import static uk.ac.ox.poseidon.agents.vessels.extractors.Factories.availableHoldCapacityInKg;
import static uk.ac.ox.poseidon.agents.vessels.extractors.Factories.currentTripDuration;
import static uk.ac.ox.poseidon.core.aggregators.Factories.meanAggregator;
import static uk.ac.ox.poseidon.core.predicates.Factories.condition;
import static uk.ac.ox.poseidon.core.predicates.comparable.Factories.lessThan;
import static uk.ac.ox.poseidon.core.predicates.logical.Factories.allOf;
import static uk.ac.ox.poseidon.core.predicates.logical.Factories.alwaysTrue;
import static uk.ac.ox.poseidon.core.predicates.numeric.Factories.greaterThan;
import static uk.ac.ox.poseidon.core.providers.constant.Factories.constant;
import static uk.ac.ox.poseidon.core.providers.constant.Factories.constantDouble;
import static uk.ac.ox.poseidon.core.providers.random.Factories.randomDouble;
import static uk.ac.ox.poseidon.core.providers.random.Factories.randomInt;
import static uk.ac.ox.poseidon.core.providers.temporal.Factories.currentDateTime;
import static uk.ac.ox.poseidon.biology.biomass.Factories.*;
import static uk.ac.ox.poseidon.biology.species.Factories.species;
import static uk.ac.ox.poseidon.core.quantities.Factories.*;
import static uk.ac.ox.poseidon.core.schedule.Factories.*;
import static uk.ac.ox.poseidon.core.time.Factories.*;
import static uk.ac.ox.poseidon.core.utils.Factories.*;
import static uk.ac.ox.poseidon.geography.grids.extractors.Factories.cellValue;
import static uk.ac.ox.poseidon.geography.grids.suppliers.Factories.gridSum;
import static uk.ac.ox.poseidon.geography.paths.Factories.pathFinder;
import static uk.ac.ox.poseidon.geography.predicates.Factories.inDepthRange;
import static uk.ac.ox.poseidon.geography.predicates.Factories.isActiveWaterCell;
import static uk.ac.ox.poseidon.geography.utils.Factories.elevationTable;
import static uk.ac.ox.poseidon.io.paths.Factories.path;
import static uk.ac.ox.poseidon.io.sources.Factories.zipEntryDataSource;
import static uk.ac.ox.poseidon.io.tables.Factories.*;

public class PeterSnapperScenario implements Supplier<Scenario> {

    private static final Path INPUT_PATH =
        Path.of("POSEIDON", "examples", "inputs", "peter_snapper");

    private static final double LEARNING_ALPHA = 1;
    private static final double EXPLORATION_PROBABILITY = 0.2;
    private static final int TOTAL_CARRYING_CAPACITY = 110_749_315;
    private static final String CURRENCY_CODE = "IDR";

    static void main() {

        // This just checks that the biomass growth matches that of the old petersnapper scenario.
        // It does; so the calibration mismatch is from fisher behaviour, not biology.

        final Scenario scenario = new PeterSnapperScenario().get();

        final Path outputPath = Path.of("POSEIDON", "examples", "outputs", "peter_snapper");
        new ScenarioWriter().write(scenario, outputPath.resolve("scenario.yaml"));

        @SuppressWarnings("RedundantTypeArguments") final Simulation simulation =
            scenario.startNewSimulation(
                uk.ac.ox.poseidon.core.SimulationStartOptions
                    .builder()
                    .seed(0)
                    .propertyOverride(
                        "components(agentCreators).steppable.steppables.factories[1]" +
                            ".numberOfVesselsToCreate",
                        0
                    )
                    .extraComponent(
                        "totalLandingsPerYear",
                        finalProcess(
                            csvTableWriter(
                                tableFromMap(
                                    tableDefinition(
                                        columnDefinition("year", "INTEGER"),
                                        columnDefinition("landings", "DOUBLE")
                                    ),
                                    new TotalLandingsPerYearAccumulatorFactory()
                                ),
                                path(outputPath.resolve("landings.csv")),
                                false,
                                true
                            )
                        )
                    )
                    .extraComponent(
                        "biomass_per_year",
                        finalProcess(
                            csvTableWriter(
                                scheduledRepeatingFromStart(
                                    YEARLY,
                                    steppableTable(
                                        tableDefinition(
                                            columnDefinition("date_time", "LOCAL_DATE_TIME"),
                                            columnDefinition("total_biomass", "DOUBLE")
                                        ),
                                        currentDateTime(),
                                        gridSum(scenario.<DoubleGrid>component("biomassGrid"))
                                    )
                                ),
                                path(outputPath.resolve("biomass.csv")),
                                false,
                                true
                            )
                        )
                    )
                    .build()
            );
        try {
            final TemporalSchedule temporalSchedule = simulation.getTemporalSchedule();
            for (int i = 0; i < 10; i++) {
                temporalSchedule.stepFor(simulation, Period.ofYears(1));
                System.out.println(simulation.getComponent(BiomassGrid.class).getSum());
            }
        } finally {
            simulation.finish();
        }

    }

    @Override
    public Scenario get() {
        final Scenario.ScenarioBuilder builder = Scenario.builder();

        final var inputPath = path(INPUT_PATH);
        final var startingDateTime = startOf(LocalDate.of(2000, 1, 1));
        final var elevationTable =
            elevationTable(
                csvTableFrom(zipEntryDataSource(
                    inputPath.plus("elevations.zip"),
                    "elevations.csv"
                )),
                "lon",
                "lat",
                "elevation"
            );

        final var modelGrid =
            new ModelGridFromLonLatTableFactory<>(elevationTable, 70, 0.000001);

        final var bathymetricGrid =
            new BathymetricGridFromElevationTableFactory<>(
                elevationTable,
                modelGrid,
                meanAggregator(),
                false
            );

        final var carryingCapacityGrid =
            carryingCapacityGrid(
                new NormalisedDoubleGridFromAllocatorFactory<>(
                    modelGrid,
                    filteredAllocator(
                        supplierAllocator(constantDouble(1.0)),
                        allOf(
                            isActiveWaterCell(bathymetricGrid),
                            inDepthRange(bathymetricGrid, 30, 800)
                        )
                    ),
                    kilograms(TOTAL_CARRYING_CAPACITY)
                )
            );

        final SpeciesFactory species = species("PS", "Peter Snapper", null);
        final var biomassGrid =
            biomassGrid(
                modelGrid,
                species,
                proportionOfCarryingCapacityAllocator(
                    carryingCapacityGrid,
                    randomDouble(0.7, 0.8)
                )
            );

        final var biomassDiffuser =
            scheduledRepeating(
                dateTimeAfterStarting(ONE_DAY),
                DAILY,
                biomassDiffuser(
                    biomassGrid,
                    carryingCapacityGrid,
                    smoothBiomassDiffusionRule(
                        0.001,
                        0.01
                    )
                ),
                0
            );

        final var biomassGrower =
            scheduledRepeating(
                dateTimeAfterStarting(ONE_YEAR),
                YEARLY,
                commonBiomassGrower(
                    biomassGrid,
                    carryingCapacityGrid,
                    logisticGrowthRule(0.372),
                    randomBiomassRecruitmentAllocator()
                ),
                0
            );

        final var distance = haversineDistanceCalculator(modelGrid);
        final PortFactory benoa = new PortFactory("P1", "Benoa");
        final PortFactory kupang = new PortFactory("P2", "Kupang");
        final var portGrid =
            new PortGridFactory<>(
                listOf(
                    new PairFactory<>(benoa, new CoordinateFactory(115.238843, -8.799605)),
                    new PairFactory<>(kupang, new CoordinateFactory(123.586249, -10.148044))
                ),
                bathymetricGrid,
                distance
            );

        final var catchCategory = uncategorisedCatchCategory();
        final var marketGrid = new MarketGridFactory<>(
            portGrid,
            new OneBiomassMarketPerPortFactory(
                portGrid,
                listOf(
                    new PriceEntryFactory<>(
                        catchCategory,
                        species,
                        new PriceFactory(40000.0, CURRENCY_CODE, "kg")
                    )
                )
            )
        );

        final var fuelStationGrid = fuelStationGrid(
            portGrid,
            oneFuelStationPerPort(
                portGrid,
                money(10_000, CURRENCY_CODE),
                200
            )
        );

        final var vesselField = vesselField(modelGrid);

        final var pathFinder =
            pathFinder(
                bathymetricGrid,
                portGrid,
                distance
            );

        final var gear =
            new FixedBiomassProportionGearFactory<>(
                "FGL", // droplines count as "fixed gears and lines"
                0.000641964,
                constant(ONE_HOUR)
            );

        final var optionValuesRegister =
            new ComponentRegisterFactory<MutableOptionValues<Int2D>>();

        final var optionValues =
            component(
                new ExponentialMovingAverageOptionValuesFactory<>(LEARNING_ALPHA),
                optionValuesRegister
            );

        final EpsilonGreedyDestinationSupplierFactory destinationSupplier =
            new EpsilonGreedyDestinationSupplierFactory(
                EXPLORATION_PROBABILITY,
                new NeighbourhoodGridExplorerFactory(
                    optionValues,
                    condition(
                        cellValue(carryingCapacityGrid),
                        greaterThan(0)
                    ),
                    pathFinder,
                    randomInt(1, 10)
                ),
                new ImitatingPickerFactory<>(
                    optionValues,
                    alwaysTrue(),
                    new BestOptionsFromFriendsSupplierFactory<>(
                        5,
                        optionValuesRegister
                    )
                )
            );

        final var tripEvaluator =
            tripEvaluator(
                optionValues,
                profitPerHour(CURRENCY_CODE)
            );

        final var behaviour =
            new BehaviourFactory(
                sequenceTask(
                    startTrip(destinationSupplier),
                    new TravelAlongPathFactory(pathFinder, distance),
                    untilFail(
                        sequenceTask(
                            new FishingFactory(
                                currentCellFisheable(biomassGrid),
                                new ProportionallyLimitingBiomassToHoldFactory()
                            ),
                            checkThat(
                                allOf(
                                    condition(availableHoldCapacityInKg(), greaterThan(1)),
                                    condition(currentTripDuration(), lessThan(days(10)))
                                )
                            )
                        )
                    ),
                    new SetDestinationToOriginFactory(),
                    new TravelAlongPathFactory(pathFinder, distance),
                    new LandCatchesFactory(constant(ONE_HOUR)),
                    refuel(fuelStationGrid),
                    new EndTripFactory(),
                    waitFor(constant(hours(12)))
                )
            );

        final var agentCreators =
            scheduledOnceAtStart(
                new SteppableSequenceFactory(
                    mappedFactory(
                        new VesselCreatorFactory(
                            vesselField,
                            portGrid,
                            marketGrid,
                            new PrefixedIdSupplierFactory("V"),
                            new PrefixedIdFactory("Vessel "),
                            account(),
                            null, // mapped over
                            standardBiomassHold(
                                massOf(15_000, KILOGRAM),
                                massOf(1, KILOGRAM),
                                uniformCatchCategoriser(catchCategory)
                            ),
                            gear,
                            new SimpleEngineFactory<>(
                                fullTank(volumeOf(100000, LITRE)),
                                speedOf(16, KILOMETRE_PER_HOUR),
                                volumeOf(3, LITRE)
                            ),
                            behaviour, // new InactiveBehaviourFactory(),
                            List.of(tripEvaluator),
                            0 // mapped over
                        ),
                        mappedProperty(
                            VesselCreatorFactory::setHomePort,
                            List.of(benoa, kupang)
                        ),
                        mappedProperty(
                            VesselCreatorFactory::setNumberOfVesselsToCreate,
                            List.of(25, 50)
                        )
                    )
                ),
                0
            );

        return builder
            .startingDateTime(startingDateTime)
            .component("modelGrid", modelGrid)
            .component("bathymetricGrid", bathymetricGrid)
            .component("carryingCapacityGrid", carryingCapacityGrid)
            .component("biomassGrid", biomassGrid)
            .component("biomassDiffuser", biomassDiffuser)
            .component("biomassGrower", biomassGrower)
            .component("portGrid", portGrid)
            .component("marketGrid", marketGrid)
            .component("gear", gear)
            .component("vesselField", vesselField)
            .component("agentCreators", agentCreators)
            .build();
    }
}
