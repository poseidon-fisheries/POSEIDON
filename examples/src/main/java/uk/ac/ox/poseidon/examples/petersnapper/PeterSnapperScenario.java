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
import uk.ac.ox.poseidon.agents.choices.EpsilonGreedyDestinationSupplierFactory;
import uk.ac.ox.poseidon.agents.choices.MutableOptionValues;
import uk.ac.ox.poseidon.agents.components.VesselComponentRegisterFactory;
import uk.ac.ox.poseidon.agents.vessels.VesselCreatorFactory;
import uk.ac.ox.poseidon.biology.species.SpeciesFactory;
import uk.ac.ox.poseidon.core.Scenario;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.schedule.TemporalSchedule;
import uk.ac.ox.poseidon.geography.grids.DoubleGrid;
import uk.ac.ox.poseidon.io.ScenarioWriter;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.function.Supplier;

import static tech.units.indriya.unit.Units.*;
import static uk.ac.ox.poseidon.agents.catches.Factories.uncategorisedCatchCategory;
import static uk.ac.ox.poseidon.agents.catches.Factories.uniformCatchCategoriser;
import static uk.ac.ox.poseidon.agents.catches.disposition.Factories.proportionallyLimitingBiomassToHold;
import static uk.ac.ox.poseidon.agents.choices.Factories.*;
import static uk.ac.ox.poseidon.agents.choices.evaluation.Factories.profitPerHour;
import static uk.ac.ox.poseidon.agents.choices.evaluation.Factories.tripEvaluator;
import static uk.ac.ox.poseidon.agents.components.Factories.registeredVesselComponent;
import static uk.ac.ox.poseidon.agents.components.Factories.vesselComponentRegister;
import static uk.ac.ox.poseidon.agents.fields.Factories.vesselField;
import static uk.ac.ox.poseidon.agents.fisheables.Factories.currentCellFisheable;
import static uk.ac.ox.poseidon.agents.fuel.Factories.fuelStationGrid;
import static uk.ac.ox.poseidon.agents.fuel.Factories.oneFuelStationPerPort;
import static uk.ac.ox.poseidon.agents.market.Factories.*;
import static uk.ac.ox.poseidon.agents.money.Factories.money;
import static uk.ac.ox.poseidon.agents.tasks.Factories.behaviour;
import static uk.ac.ox.poseidon.agents.tasks.branches.Factories.sequenceTask;
import static uk.ac.ox.poseidon.agents.tasks.decorators.Factories.untilFail;
import static uk.ac.ox.poseidon.agents.tasks.destinations.Factories.startTrip;
import static uk.ac.ox.poseidon.agents.tasks.fishing.Factories.fishing;
import static uk.ac.ox.poseidon.agents.tasks.general.Factories.checkThat;
import static uk.ac.ox.poseidon.agents.tasks.general.Factories.waitFor;
import static uk.ac.ox.poseidon.agents.tasks.landings.Factories.landCatches;
import static uk.ac.ox.poseidon.agents.tasks.travel.Factories.*;
import static uk.ac.ox.poseidon.agents.vessels.Factories.*;
import static uk.ac.ox.poseidon.agents.vessels.accounts.Factories.account;
import static uk.ac.ox.poseidon.agents.vessels.engines.Factories.fullTank;
import static uk.ac.ox.poseidon.agents.vessels.engines.Factories.simpleEngine;
import static uk.ac.ox.poseidon.agents.vessels.extractors.Factories.availableHoldCapacityInKg;
import static uk.ac.ox.poseidon.agents.vessels.extractors.Factories.currentTripDuration;
import static uk.ac.ox.poseidon.agents.vessels.friends.Factories.dynamicFriendsSupplier;
import static uk.ac.ox.poseidon.agents.vessels.gears.Factories.fixedBiomassProportionGear;
import static uk.ac.ox.poseidon.agents.vessels.holds.Factories.standardBiomassHold;
import static uk.ac.ox.poseidon.agents.vessels.predicates.Factories.vesselHasSameHomePort;
import static uk.ac.ox.poseidon.agents.vessels.predicates.Factories.vesselIsActive;
import static uk.ac.ox.poseidon.agents.vessels.providers.Factories.accessibleWaterCells;
import static uk.ac.ox.poseidon.biology.allocators.Factories.proportionOfCarryingCapacityAllocator;
import static uk.ac.ox.poseidon.biology.biomass.Factories.*;
import static uk.ac.ox.poseidon.biology.species.Factories.species;
import static uk.ac.ox.poseidon.core.aggregators.Factories.meanAggregator;
import static uk.ac.ox.poseidon.core.predicates.Factories.condition;
import static uk.ac.ox.poseidon.core.predicates.comparable.Factories.lessThan;
import static uk.ac.ox.poseidon.core.predicates.logical.Factories.allOf;
import static uk.ac.ox.poseidon.core.predicates.logical.Factories.alwaysTrue;
import static uk.ac.ox.poseidon.core.predicates.numeric.Factories.greaterThan;
import static uk.ac.ox.poseidon.core.providers.Factories.firstIntFrom;
import static uk.ac.ox.poseidon.core.providers.constant.Factories.constant;
import static uk.ac.ox.poseidon.core.providers.constant.Factories.constantDouble;
import static uk.ac.ox.poseidon.core.providers.random.Factories.randomDouble;
import static uk.ac.ox.poseidon.core.providers.random.Factories.randomInt;
import static uk.ac.ox.poseidon.core.providers.temporal.Factories.currentDateTime;
import static uk.ac.ox.poseidon.core.quantities.Factories.*;
import static uk.ac.ox.poseidon.core.quantities.VolumetricFlowRateFactory.LITRE_PER_HOUR;
import static uk.ac.ox.poseidon.core.schedule.Factories.*;
import static uk.ac.ox.poseidon.core.time.Factories.*;
import static uk.ac.ox.poseidon.core.utils.Factories.*;
import static uk.ac.ox.poseidon.examples.petersnapper.Factories.totalLandingsPerYearAccumulator;
import static uk.ac.ox.poseidon.geography.Factories.coordinate;
import static uk.ac.ox.poseidon.geography.allocators.Factories.filteredAllocator;
import static uk.ac.ox.poseidon.geography.allocators.Factories.supplierAllocator;
import static uk.ac.ox.poseidon.geography.bathymetry.Factories.bathymetricGridFromElevationTable;
import static uk.ac.ox.poseidon.geography.distance.Factories.haversineDistanceCalculator;
import static uk.ac.ox.poseidon.geography.grids.Factories.modelGridFromLonLatTable;
import static uk.ac.ox.poseidon.geography.grids.Factories.normalisedDoubleGridFromAllocator;
import static uk.ac.ox.poseidon.geography.grids.extractors.Factories.cellValue;
import static uk.ac.ox.poseidon.geography.grids.suppliers.Factories.gridSum;
import static uk.ac.ox.poseidon.geography.paths.Factories.pathFinder;
import static uk.ac.ox.poseidon.geography.ports.Factories.port;
import static uk.ac.ox.poseidon.geography.ports.Factories.portGrid;
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
/*                    .propertyOverride(
                        "components(agentCreators).steppable.steppables.factories[0]" +
                            ".numberOfVesselsToCreate",
                        5
                    )
                    .propertyOverride(
                        "components(agentCreators).steppable.steppables.factories[1]" +
                            ".numberOfVesselsToCreate",
                        0
                    )*/
                    .extraComponent(
                        "totalLandingsPerYear",
                        finalProcess(
                            csvTableWriter(
                                tableFromMap(
                                    tableDefinition(
                                        columnDefinition("year", "INTEGER"),
                                        columnDefinition("landings", "DOUBLE")
                                    ),
                                    totalLandingsPerYearAccumulator()
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
            modelGridFromLonLatTable(elevationTable, 70, 0.000001);

        final var bathymetricGrid =
            bathymetricGridFromElevationTable(
                elevationTable,
                modelGrid,
                meanAggregator(),
                false
            );

        final var carryingCapacityGrid =
            carryingCapacityGrid(
                normalisedDoubleGridFromAllocator(
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
        final var benoa = port("P1", "Benoa");
        final var kupang = port("P2", "Kupang");
        final var portGrid =
            portGrid(
                listOf(
                    pair(benoa, coordinate(115.238843, -8.799605)),
                    pair(kupang, coordinate(123.586249, -10.148044))
                ),
                bathymetricGrid,
                distance
            );

        final var catchCategory = uncategorisedCatchCategory();
        final var marketGrid = marketGrid(
            portGrid,
            oneBiomassMarketPerPort(
                portGrid,
                listOf(
                    priceEntry(
                        catchCategory,
                        species,
                        price(40000.0, CURRENCY_CODE, "kg")
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
            fixedBiomassProportionGear(
                "FGL", // droplines count as "fixed gears and lines"
                0.000641964,
                constant(ONE_HOUR),
                volumetricFlowRateOf(70.0, LITRE_PER_HOUR)
            );

        final VesselComponentRegisterFactory<MutableOptionValues<Int2D>>
            optionValuesRegister = vesselComponentRegister();

        final var optionValues =
            registeredVesselComponent(
                exponentialMovingAverageOptionValues(LEARNING_ALPHA),
                optionValuesRegister
            );

        final var validCellPredicate =
            condition(
                cellValue(carryingCapacityGrid),
                greaterThan(0)
            );
        final EpsilonGreedyDestinationSupplierFactory destinationSupplier =
            epsilonGreedyDestination(
                EXPLORATION_PROBABILITY,
                neighbourhoodGridExplorer(
                    optionValues,
                    pathFinder,
                    validCellPredicate,
                    firstIntFrom(perVessel(randomInt(1, 10))),
                    randomGridExplorer(accessibleWaterCells(pathFinder), validCellPredicate)
                ),
                imitatingPicker(
                    optionValues,
                    alwaysTrue(),
                    bestOptionsFromFriends(
                        optionValuesRegister,
                        dynamicFriendsSupplier(
                            2,
                            optionValuesRegister,
                            allOf(
                                vesselIsActive(),
                                vesselHasSameHomePort()
                            )
                        )
                    )
                )
            );

        final var tripEvaluator =
            tripEvaluator(
                optionValues,
                profitPerHour(CURRENCY_CODE)
            );

        final var behaviour =
            behaviour(
                sequenceTask(
                    startTrip(destinationSupplier),
                    travelAlongPath(pathFinder, distance),
                    untilFail(
                        sequenceTask(
                            fishing(
                                currentCellFisheable(biomassGrid),
                                proportionallyLimitingBiomassToHold()
                            ),
                            checkThat(
                                allOf(
                                    condition(availableHoldCapacityInKg(), greaterThan(1)),
                                    condition(currentTripDuration(), lessThan(days(10)))
                                )
                            )
                        )
                    ),
                    setDestinationToOrigin(),
                    travelAlongPath(pathFinder, distance),
                    landCatches(constant(ONE_HOUR)),
                    refuel(fuelStationGrid),
                    endTrip(),
                    waitFor(constant(hours(12)))
                )
            );

        final var agentCreators =
            scheduledOnceAtStart(
                steppableSequence(
                    mappedFactory(
                        vesselCreator(
                            vesselField,
                            portGrid,
                            marketGrid,
                            prefixedIdSupplier("V"),
                            prefixedId("Vessel "),
                            account(),
                            null, // mapped over
                            standardBiomassHold(
                                massOf(15_000, KILOGRAM),
                                massOf(1, KILOGRAM),
                                uniformCatchCategoriser(catchCategory)
                            ),
                            gear,
                            simpleEngine(
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
