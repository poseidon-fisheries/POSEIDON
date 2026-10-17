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

import static java.time.Period.ofDays;
import static java.time.Period.ofYears;
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
import static uk.ac.ox.poseidon.agents.tasks.branches.Factories.sequenceTask;
import static uk.ac.ox.poseidon.agents.tasks.decorators.Factories.untilFail;
import static uk.ac.ox.poseidon.agents.tasks.fishing.Factories.fishing;
import static uk.ac.ox.poseidon.agents.tasks.general.Factories.checkThat;
import static uk.ac.ox.poseidon.agents.tasks.general.Factories.waitFor;
import static uk.ac.ox.poseidon.agents.tasks.landings.Factories.landCatches;
import static uk.ac.ox.poseidon.agents.tasks.travel.Factories.*;
import static uk.ac.ox.poseidon.agents.vessels.Factories.*;
import static uk.ac.ox.poseidon.agents.vessels.accounts.Factories.account;
import static uk.ac.ox.poseidon.agents.vessels.engines.Factories.simpleEngine;
import static uk.ac.ox.poseidon.agents.vessels.friends.Factories.dynamicFriends;
import static uk.ac.ox.poseidon.agents.vessels.gears.Factories.fixedBiomassProportionGear;
import static uk.ac.ox.poseidon.agents.vessels.holds.Factories.standardBiomassHold;
import static uk.ac.ox.poseidon.agents.vessels.tanks.Factories.fullTank;
import static uk.ac.ox.poseidon.agents.vessels.values.Factories.accessibleWaterCells;
import static uk.ac.ox.poseidon.agents.vessels.values.Factories.componentProperty;
import static uk.ac.ox.poseidon.agents.vessels.values.Factories.currentTimeAtSea;
import static uk.ac.ox.poseidon.agents.vessels.values.Factories.currentTripDestinationCell;
import static uk.ac.ox.poseidon.agents.vessels.values.Factories.currentTripEventManager;
import static uk.ac.ox.poseidon.agents.vessels.values.Factories.other;
import static uk.ac.ox.poseidon.agents.vessels.values.Factories.vesselIsActive;
import static uk.ac.ox.poseidon.agents.vessels.values.Factories.vesselProperty;
import static uk.ac.ox.poseidon.biology.allocators.Factories.proportionOfCarryingCapacityAllocator;
import static uk.ac.ox.poseidon.biology.biomass.Factories.*;
import static uk.ac.ox.poseidon.biology.species.Factories.species;
import static uk.ac.ox.poseidon.core.aggregators.Factories.meanAggregator;
import static uk.ac.ox.poseidon.core.predicates.Factories.condition;
import static uk.ac.ox.poseidon.core.predicates.Factories.equal;
import static uk.ac.ox.poseidon.core.predicates.comparable.Factories.greaterThan;
import static uk.ac.ox.poseidon.core.predicates.comparable.Factories.lessThan;
import static uk.ac.ox.poseidon.core.predicates.logical.Factories.allOf;
import static uk.ac.ox.poseidon.core.providers.constant.Factories.always;
import static uk.ac.ox.poseidon.core.providers.constant.Factories.constant;
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

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.function.Supplier;
import sim.util.Int2D;
import uk.ac.ox.poseidon.agents.choices.EpsilonGreedyDestinationFactory;
import uk.ac.ox.poseidon.agents.choices.MutableOptionValues;
import uk.ac.ox.poseidon.agents.components.VesselComponentRegisterFactory;
import uk.ac.ox.poseidon.biology.species.SpeciesFactory;
import uk.ac.ox.poseidon.core.Scenario;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.schedule.TemporalSchedule;
import uk.ac.ox.poseidon.io.ScenarioWriter;

/**
 * A worked example scenario: a single-species, single-gear artisanal fishery
 * (modelled on Indonesia's Peter Snapper fishery/tutorial) with two ports,
 * epsilon-greedy destination choice (with imitation of nearby vessels' best
 * options), a fixed-proportion gear, and logistic biomass growth with
 * diffusion.
 */
public class PeterSnapperScenario implements Supplier<Scenario> {

  // Relative to the examples module, which Gradle runs from: set IDE run
  // configurations to the module's working directory.
  private static final Path INPUT_PATH = Path.of("inputs", "peter_snapper");

  private static final double LEARNING_ALPHA = 1;
  private static final double EXPLORATION_PROBABILITY = 0.2;
  private static final int MAXIMUM_EXPLORATION_WIDENING = 3;
  private static final int TOTAL_CARRYING_CAPACITY = 110_749_315;
  private static final String CURRENCY_CODE = "IDR";

  static void main() {
    // This just checks that the biomass growth matches that of the old
    // petersnapper scenario. It does; so the calibration mismatch is from
    // fisher behaviour, not biology.

    final Scenario scenario = new PeterSnapperScenario().get();

    final Path outputPath = Path.of("outputs", "peter_snapper");
    new ScenarioWriter().write(scenario, outputPath.resolve("scenario.yaml"));

    final Simulation simulation = scenario
      .withComponent(
        "totalLandingsPerYear",
        finalProcess(
          csvTableWriter(
            tableFromMap(
              tableDefinition(
                columnDefinition("year", "INTEGER"),
                columnDefinition("landings_kg", "DOUBLE")
              ),
              totalLandingsPerYearAccumulator()
            ),
            path(outputPath.resolve("landings.csv")),
            false,
            true
          )
        )
      )
      .withComponent(
        "biomass_per_year",
        finalProcess(
          csvTableWriter(
            scheduledRepeatingFromStart(
              period(ofYears(1)),
              steppableTable(
                tableDefinition(
                  columnDefinition("date_time", "LOCAL_DATE_TIME"),
                  columnDefinition("total_biomass", "DOUBLE")
                ),
                currentDateTime(),
                gridSum(scenario.component("biomassGrid"))
              )
            ),
            path(outputPath.resolve("biomass.csv")),
            false,
            true
          )
        )
      )
      .startNewSimulation(0);
    try {
      final TemporalSchedule temporalSchedule =
        simulation.getTemporalSchedule();
      for (int i = 0; i < 10; i++) {
        temporalSchedule.stepFor(simulation, Period.ofYears(1));
      }
    } finally {
      simulation.finish();
    }
  }

  /** @return a freshly built Peter Snapper {@link Scenario} */
  @Override
  public Scenario get() {
    final Scenario.ScenarioBuilder builder = Scenario.builder();

    final var inputPath = path(INPUT_PATH);
    final var startingDateTime = startOf(LocalDate.of(1, 1, 1));
    final var elevationTable = elevationTable(
      tableFromCsv(
        zipEntryDataSource(inputPath.plus("elevations.zip"), "elevations.csv")
      ),
      "lon",
      "lat",
      "elevation"
    );

    final var modelGrid = modelGridFromLonLatTable(
      elevationTable,
      70,
      0.000001
    );

    final var bathymetricGrid = bathymetricGridFromElevationTable(
      elevationTable,
      modelGrid,
      meanAggregator(),
      false
    );

    final var carryingCapacityGrid = carryingCapacityGrid(
      normalisedDoubleGridFromAllocator(
        modelGrid,
        filteredAllocator(
          supplierAllocator(constant(1.0)),
          allOf(
            isActiveWaterCell(bathymetricGrid),
            inDepthRange(bathymetricGrid, 30, 800)
          )
        ),
        kilograms(TOTAL_CARRYING_CAPACITY)
      )
    );

    final SpeciesFactory species = species("PS", "Peter Snapper", null);
    final var biomassGrid = biomassGrid(
      modelGrid,
      species,
      proportionOfCarryingCapacityAllocator(
        carryingCapacityGrid,
        randomDouble(0.7, 0.8)
      )
    );

    final var biomassDiffuser = scheduledRepeating(
      dateTimeAfterStarting(days(1)),
      period(ofDays(1)),
      biomassDiffuser(
        biomassGrid,
        carryingCapacityGrid,
        smoothBiomassDiffusionRule(0.001, 0.01)
      ),
      0
    );

    final var biomassGrower = scheduledRepeating(
      dateTimeAfterStarting(period(ofYears(1))),
      period(ofYears(1)),
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
    final var portGrid = portGrid(
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
      oneBiomassMarketPerPort(portGrid)
    );

    final var initialPrices = initialPrices(
      uniformMarketPrices(
        marketGrid,
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
      oneFuelStationPerPort(portGrid, money(10_000, CURRENCY_CODE), 200)
    );

    final var vesselField = vesselField(modelGrid);

    final var pathFinder = pathFinder(bathymetricGrid, portGrid, distance);

    // The proportion is a scenario component, so that calibration can vary it
    final var gearProportion = object(0.000641964);

    final var gear = vesselComponent(
      fixedBiomassProportionGear(
        gearProportion,
        massOf(1, KILOGRAM),
        constant(hours(1)),
        volumetricFlowRateOf(70.0, LITRE_PER_HOUR)
      )
    );

    final var hold = vesselComponent(
      standardBiomassHold(
        massOf(15_000, KILOGRAM),
        massOf(1, KILOGRAM),
        uniformCatchCategoriser(catchCategory)
      )
    );

    final var engine = vesselComponent(
      simpleEngine(speedOf(16, KILOMETRE_PER_HOUR), volumeOf(3, LITRE))
    );

    final var fuelTank = vesselComponent(fullTank(volumeOf(100000, LITRE)));

    final VesselComponentRegisterFactory<
      MutableOptionValues<Int2D>
    > optionValuesRegister = vesselComponentRegister();

    final var optionValues = registeredVesselComponent(
      exponentialMovingAverageOptionValues(LEARNING_ALPHA),
      optionValuesRegister
    );

    final var validCellPredicate = condition(
      cellValue(carryingCapacityGrid),
      greaterThan(0)
    );
    final EpsilonGreedyDestinationFactory destination =
      epsilonGreedyDestination(
        EXPLORATION_PROBABILITY,
        neighbourhoodGridExplorer(
          pathFinder,
          validCellPredicate,
          oneValueFrom(randomInt(1, 10)),
          MAXIMUM_EXPLORATION_WIDENING,
          bestKnownOption(
            optionValues,
            randomGridExplorer(
              accessibleWaterCells(pathFinder),
              validCellPredicate
            )
          )
        ),
        imitatingPicker(
          optionValues,
          always(true),
          bestOptionsFromFriends(
            optionValuesRegister,
            dynamicFriends(
              2,
              optionValuesRegister,
              allOf(
                other(vesselIsActive()),
                condition(
                  other(vesselProperty("homePort")),
                  equal(vesselProperty("homePort"))
                )
              )
            )
          )
        )
      );

    final var tripEvaluator = vesselComponent(
      tripEvaluator(optionValues, profitPerHour(CURRENCY_CODE))
    );

    final var behaviour = behaviour(
      sequenceTask(
        trip(
          destination,
          travelAlongPathTo(
            engine,
            fuelTank,
            pathFinder,
            distance,
            currentTripDestinationCell(),
            currentTripEventManager()
          ),
          untilFail(
            sequenceTask(
              fishing(
                gear,
                hold,
                fuelTank,
                currentCellFisheable(biomassGrid),
                proportionallyLimitingBiomassToHold()
              ),
              checkThat(
                allOf(
                  condition(
                    componentProperty(hold, "availableCapacityInKg"),
                    greaterThan(1)
                  ),
                  condition(currentTimeAtSea(), lessThan(constant(days(10))))
                )
              )
            )
          ),
          setDestinationToOrigin(),
          travelAlongPathTo(
            engine,
            fuelTank,
            pathFinder,
            distance,
            currentTripDestinationCell(),
            currentTripEventManager()
          ),
          arriveInPort(),
          landCatches(hold, constant(hours(1))),
          refuel(fuelTank, fuelStationGrid)
        ),
        waitFor(constant(hours(12)))
      )
    );

    final var benoaVesselCreator = vesselCreator(
      vesselField,
      portGrid,
      marketGrid,
      prefixedIdSupplier("V"),
      prefixedId("Vessel "),
      account(),
      benoa,
      behaviour,
      List.of(gear, engine, fuelTank, hold, tripEvaluator),
      25
    );
    // Kupang's vessels are like Benoa's, but more of them
    final var kupangVesselCreator = benoaVesselCreator
      .toBuilder()
      .homePort(kupang)
      .numberOfVesselsToCreate(50)
      .build();

    final var agentCreators = scheduledOnceAtStart(
      steppableSequence(listOf(benoaVesselCreator, kupangVesselCreator)),
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
      .component("initialPrices", initialPrices)
      .component("gearProportion", gearProportion)
      .component("vesselField", vesselField)
      .component("agentCreators", agentCreators)
      .build();
  }
}
