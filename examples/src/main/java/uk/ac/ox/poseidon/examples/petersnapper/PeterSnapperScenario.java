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
import uk.ac.ox.poseidon.agents.catches.UncategorisedCatchCategoryFactory;
import uk.ac.ox.poseidon.agents.catches.UniformCatchCategoriserFactory;
import uk.ac.ox.poseidon.agents.catches.disposition.ProportionallyLimitingBiomassToHoldFactory;
import uk.ac.ox.poseidon.agents.choices.*;
import uk.ac.ox.poseidon.agents.components.ComponentFactory;
import uk.ac.ox.poseidon.agents.components.ComponentRegisterFactory;
import uk.ac.ox.poseidon.agents.fields.VesselFieldFactory;
import uk.ac.ox.poseidon.agents.fisheables.CurrentCellFisheableFactory;
import uk.ac.ox.poseidon.agents.market.MarketGridFactory;
import uk.ac.ox.poseidon.agents.market.OneBiomassMarketPerPortFactory;
import uk.ac.ox.poseidon.agents.market.PriceEntryFactory;
import uk.ac.ox.poseidon.agents.market.PriceFactory;
import uk.ac.ox.poseidon.agents.tasks.BehaviourFactory;
import uk.ac.ox.poseidon.agents.tasks.branches.SequenceTaskFactory;
import uk.ac.ox.poseidon.agents.tasks.destinations.StartTripFactory;
import uk.ac.ox.poseidon.agents.tasks.fishing.FishingFactory;
import uk.ac.ox.poseidon.agents.tasks.landings.LandCatchesFactory;
import uk.ac.ox.poseidon.agents.tasks.travel.RoundTripFactory;
import uk.ac.ox.poseidon.agents.tasks.travel.TravelAlongPathFactory;
import uk.ac.ox.poseidon.agents.vessels.PrefixedIdFactory;
import uk.ac.ox.poseidon.agents.vessels.VesselCreatorFactory;
import uk.ac.ox.poseidon.agents.vessels.accounts.AccountFactory;
import uk.ac.ox.poseidon.agents.vessels.engines.SimpleEngineFactory;
import uk.ac.ox.poseidon.agents.vessels.gears.FixedBiomassProportionGearFactory;
import uk.ac.ox.poseidon.agents.vessels.holds.InfiniteBiomassHoldFactory;
import uk.ac.ox.poseidon.biology.allocators.ProportionOfCarryingCapacityAllocatorFactory;
import uk.ac.ox.poseidon.biology.biomass.*;
import uk.ac.ox.poseidon.biology.species.SpeciesFactory;
import uk.ac.ox.poseidon.core.MappedFactory;
import uk.ac.ox.poseidon.core.Scenario;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.aggregators.MeanFactory;
import uk.ac.ox.poseidon.core.quantities.KilogramsFactory;
import uk.ac.ox.poseidon.core.quantities.MassFactory;
import uk.ac.ox.poseidon.core.quantities.SpeedFactory;
import uk.ac.ox.poseidon.core.schedule.ScheduledOnceFactory;
import uk.ac.ox.poseidon.core.schedule.ScheduledRepeatingFactory;
import uk.ac.ox.poseidon.core.schedule.SteppableSequenceFactory;
import uk.ac.ox.poseidon.core.suppliers.RandomIntSupplierFactory;
import uk.ac.ox.poseidon.core.time.DateTimeFactory;
import uk.ac.ox.poseidon.core.utils.ListFactory;
import uk.ac.ox.poseidon.core.utils.PairFactory;
import uk.ac.ox.poseidon.core.utils.PrefixedIdSupplierFactory;
import uk.ac.ox.poseidon.geography.CoordinateFactory;
import uk.ac.ox.poseidon.geography.allocators.FilteredAllocatorFactory;
import uk.ac.ox.poseidon.geography.allocators.SupplierAllocatorFactory;
import uk.ac.ox.poseidon.geography.bathymetry.BathymetricGridFromElevationTableFactory;
import uk.ac.ox.poseidon.geography.distance.HaversineDistanceCalculatorFactory;
import uk.ac.ox.poseidon.geography.grids.ModelGridFromLonLatTableFactory;
import uk.ac.ox.poseidon.geography.grids.NormalisedDoubleGridFromAllocatorFactory;
import uk.ac.ox.poseidon.geography.paths.DefaultPathFinderFactory;
import uk.ac.ox.poseidon.geography.ports.PortFactory;
import uk.ac.ox.poseidon.geography.ports.PortGridFactory;
import uk.ac.ox.poseidon.geography.utils.ElevationTableFactory;
import uk.ac.ox.poseidon.io.paths.PathFactory;
import uk.ac.ox.poseidon.io.tables.CsvTableFactory;

import java.nio.file.Path;
import java.time.Period;
import java.util.Map;
import java.util.function.Supplier;

import static tech.units.indriya.unit.Units.KILOGRAM;
import static uk.ac.ox.poseidon.core.predicates.Factories.adaptedPredicate;
import static uk.ac.ox.poseidon.core.predicates.logical.Factories.allOf;
import static uk.ac.ox.poseidon.core.predicates.logical.Factories.alwaysTrue;
import static uk.ac.ox.poseidon.core.predicates.numeric.Factories.above;
import static uk.ac.ox.poseidon.core.suppliers.ConstantDurationSuppliers.ONE_HOUR_DURATION_SUPPLIER;
import static uk.ac.ox.poseidon.core.suppliers.Factories.constantDouble;
import static uk.ac.ox.poseidon.core.suppliers.Factories.randomDouble;
import static uk.ac.ox.poseidon.core.time.DurationFactory.ONE_DAY;
import static uk.ac.ox.poseidon.core.utils.Factories.listOf;
import static uk.ac.ox.poseidon.geography.grids.adaptors.Factories.cellValue;
import static uk.ac.ox.poseidon.geography.predicates.Factories.inDepthRange;
import static uk.ac.ox.poseidon.geography.predicates.Factories.isActiveWaterCell;

public class PeterSnapperScenario implements Supplier<Scenario> {

    private static final Path INPUT_PATH =
        Path.of("POSEIDON", "examples", "inputs", "peter_snapper");

    private static final double LEARNING_ALPHA = 1;
    private static final double EXPLORATION_PROBABILITY = 0.2;

    public static void main(final String[] args) {
        final Simulation simulation =
            new PeterSnapperScenario().get().startNewSimulation();
        simulation
            .getTemporalSchedule()
            .stepFor(simulation, Period.ofYears(10));
        simulation.finish();
    }

    @Override
    public Scenario get() {
        final Scenario.ScenarioBuilder builder = Scenario.builder();

        final var inputPath = PathFactory.of(INPUT_PATH);
        final var startingDateTime = DateTimeFactory.ofToday();
        final var elevationTable =
            new ElevationTableFactory<>(
                CsvTableFactory.fromFile(inputPath.plus("elevations.csv")),
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
                new MeanFactory(),
                false
            );

        final var carryingCapacityGrid =
            new CarryingCapacityGridFactory<>(
                new NormalisedDoubleGridFromAllocatorFactory<>(
                    modelGrid,
                    new FilteredAllocatorFactory<>(
                        new SupplierAllocatorFactory<>(constantDouble(1.0)),
                        allOf(
                            isActiveWaterCell(bathymetricGrid),
                            inDepthRange(bathymetricGrid, 30, 800)
                        )
                    ),
                    new KilogramsFactory<>(MassFactory.of(110_749_315, KILOGRAM))
                )
            );

        final SpeciesFactory species = new SpeciesFactory("PS", "Peter Snapper", null);
        final var biomassGrid =
            new BiomassGridFactory(
                modelGrid,
                species,
                new ProportionOfCarryingCapacityAllocatorFactory<>(
                    carryingCapacityGrid,
                    randomDouble(0.7, 0.8)
                )
            );

        final var biologicalProcesses =
            new ScheduledRepeatingFactory<>(
                startingDateTime,
                ONE_DAY,
                new SteppableSequenceFactory(
                    new BiomassDiffuserFactory(
                        biomassGrid,
                        carryingCapacityGrid,
                        new SmoothBiomassDiffusionRuleFactory(
                            0.001,
                            0.01
                        )
                    ),
                    new BiomassGrowerFactory(
                        biomassGrid,
                        carryingCapacityGrid,
                        new LogisticGrowthRuleFactory(0.372)
                    )
                ),
                0
            );

        final var distance = new HaversineDistanceCalculatorFactory<>(modelGrid);
        final PortFactory benoa = new PortFactory("P1", "Benoa");
        final PortFactory kupang = new PortFactory("P2", "Kupang");
        final var portGrid =
            new PortGridFactory<>(
                ListFactory.from(
                    new PairFactory<>(benoa, new CoordinateFactory(115.238843, -8.799605)),
                    new PairFactory<>(kupang, new CoordinateFactory(123.586249, -10.148044))
                ),
                bathymetricGrid,
                distance
            );

        final UncategorisedCatchCategoryFactory catchCategory =
            new UncategorisedCatchCategoryFactory();
        final var marketGrid = new MarketGridFactory<>(
            portGrid,
            new OneBiomassMarketPerPortFactory(
                portGrid,
                ListFactory.from(
                    new PriceEntryFactory<>(
                        catchCategory,
                        species,
                        new PriceFactory(40000.0, "IDR", "kg")
                    )
                )
            )
        );

        final var vesselField = new VesselFieldFactory(modelGrid);

        final var pathFinder =
            new DefaultPathFinderFactory<>(
                bathymetricGrid,
                portGrid,
                distance
            );

        final var gear =
            new FixedBiomassProportionGearFactory<>(
                "FGL", // droplines count as "fixed gears and lines"
                0.25,
                ONE_HOUR_DURATION_SUPPLIER
            );

        final var optionValuesRegister =
            new ComponentRegisterFactory<MutableOptionValues<Int2D>>();

        final var optionValues =
            new ComponentFactory<>(
                new ExponentialMovingAverageOptionValuesFactory<>(LEARNING_ALPHA),
                optionValuesRegister
            );

        final var startTrip =
            new StartTripFactory(
                new EpsilonGreedyDestinationSupplierFactory(
                    EXPLORATION_PROBABILITY,
                    new NeighbourhoodGridExplorerFactory(
                        optionValues,
                        adaptedPredicate(
                            cellValue(carryingCapacityGrid),
                            above(0)
                        ),
                        pathFinder,
                        new RandomIntSupplierFactory(1, 10)
                    ),
                    new ImitatingPickerFactory<>(
                        optionValues,
                        alwaysTrue(),
                        new BestOptionsFromFriendsSupplierFactory<>(
                            5,
                            optionValuesRegister
                        )
                    )
                )
            );

        final var behaviour =
            new BehaviourFactory(
                SequenceTaskFactory
                    .builder()
                    .child(
                        new RoundTripFactory(
                            startTrip,
                            new TravelAlongPathFactory(
                                pathFinder,
                                distance
                            ),
                            new FishingFactory(
                                new CurrentCellFisheableFactory(biomassGrid),
                                new ProportionallyLimitingBiomassToHoldFactory()
                            ),
                            new LandCatchesFactory(
                                ONE_HOUR_DURATION_SUPPLIER
                            )
                        )
                    )
                    .build()
            );

        final var agentCreators =
            new ScheduledOnceFactory<>(
                startingDateTime,
                new SteppableSequenceFactory(
                    new MappedFactory<>(
                        new VesselCreatorFactory(
                            vesselField,
                            portGrid,
                            marketGrid,
                            new PrefixedIdSupplierFactory("V"),
                            new PrefixedIdFactory("Vessel "),
                            new AccountFactory(),
                            null, // mapped over
                            new InfiniteBiomassHoldFactory(
                                new UniformCatchCategoriserFactory<>(catchCategory)
                            ),
                            gear,
                            new SimpleEngineFactory<>(SpeedFactory.of("10 kn")),
                            behaviour, // new InactiveBehaviourFactory(),
                            0 // mapped over
                        ),
                        Map.of(
                            "homePort", ListFactory.from(benoa, kupang),
                            "numberOfVesselsToCreate", listOf(25, 50)
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
            .component("biologicalProcesses", biologicalProcesses)
            .component("portGrid", portGrid)
            .component("marketGrid", marketGrid)
            .component("vesselField", vesselField)
            .component("agentCreators", agentCreators)
            .build();
    }
}
