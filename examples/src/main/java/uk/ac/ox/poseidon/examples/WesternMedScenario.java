/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024 CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.poseidon.examples;

import lombok.Getter;
import lombok.Setter;
import sim.engine.Steppable;
import sim.util.Int2D;
import uk.ac.ox.poseidon.agents.behaviours.BehaviourFactory;
import uk.ac.ox.poseidon.agents.behaviours.WaitingBehaviourFactory;
import uk.ac.ox.poseidon.agents.behaviours.choices.BestOptionsFromFriendsSupplierFactory;
import uk.ac.ox.poseidon.agents.behaviours.choices.ExponentialMovingAverageOptionValuesFactory;
import uk.ac.ox.poseidon.agents.behaviours.choices.MutableOptionValues;
import uk.ac.ox.poseidon.agents.behaviours.destination.*;
import uk.ac.ox.poseidon.agents.behaviours.fishing.DefaultFishingBehaviourFactory;
import uk.ac.ox.poseidon.agents.behaviours.port.HomeBehaviourFactory;
import uk.ac.ox.poseidon.agents.behaviours.port.LandingBehaviourFactory;
import uk.ac.ox.poseidon.agents.behaviours.strategy.ThereAndBackBehaviourFactory;
import uk.ac.ox.poseidon.agents.behaviours.travel.TravellingAlongPathBehaviourFactory;
import uk.ac.ox.poseidon.agents.fields.VesselField;
import uk.ac.ox.poseidon.agents.fields.VesselFieldFactory;
import uk.ac.ox.poseidon.agents.fisheables.CurrentCellFisheableFactory;
import uk.ac.ox.poseidon.agents.market.BiomassMarketGridPriceFileFactory;
import uk.ac.ox.poseidon.agents.market.Market;
import uk.ac.ox.poseidon.agents.market.MarketGrid;
import uk.ac.ox.poseidon.agents.registers.Register;
import uk.ac.ox.poseidon.agents.registers.RegisterFactory;
import uk.ac.ox.poseidon.agents.registers.RegisteringFactory;
import uk.ac.ox.poseidon.agents.regulations.FishingLocationLegalityCheckerFactory;
import uk.ac.ox.poseidon.agents.regulations.Regulations;
import uk.ac.ox.poseidon.agents.tables.FishingActionListenerTableFactory;
import uk.ac.ox.poseidon.agents.vessels.AdaptedVesselPredicateFactory;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.agents.vessels.VesselScopeFactory;
import uk.ac.ox.poseidon.agents.vessels.VesselsFromFileFactory;
import uk.ac.ox.poseidon.agents.vessels.gears.FixedBiomassProportionGearFactory;
import uk.ac.ox.poseidon.agents.vessels.hold.Hold;
import uk.ac.ox.poseidon.agents.vessels.hold.ProportionalBiomassOvercapacityDiscardingStrategyFactory;
import uk.ac.ox.poseidon.agents.vessels.hold.StandardBiomassHoldFactory;
import uk.ac.ox.poseidon.biology.biomass.*;
import uk.ac.ox.poseidon.biology.species.Species;
import uk.ac.ox.poseidon.biology.species.SpeciesFromFileFactory;
import uk.ac.ox.poseidon.core.*;
import uk.ac.ox.poseidon.core.adaptors.temporal.CurrentDayOfWeekFactory;
import uk.ac.ox.poseidon.core.adaptors.temporal.CurrentTimeFactory;
import uk.ac.ox.poseidon.core.aggregators.MaxFactory;
import uk.ac.ox.poseidon.core.predicates.AdaptedPredicateFactory;
import uk.ac.ox.poseidon.core.predicates.InSetFactory;
import uk.ac.ox.poseidon.core.predicates.logical.AllOfFactory;
import uk.ac.ox.poseidon.core.predicates.logical.AnyOfFactory;
import uk.ac.ox.poseidon.core.predicates.numeric.AboveFactory;
import uk.ac.ox.poseidon.core.predicates.temporal.TimeIsAfterFactory;
import uk.ac.ox.poseidon.core.quantities.MassFactory;
import uk.ac.ox.poseidon.core.quantities.SpeedFactory;
import uk.ac.ox.poseidon.core.schedule.ScheduledRepeatingFactory;
import uk.ac.ox.poseidon.core.schedule.SteppableSequenceFactory;
import uk.ac.ox.poseidon.core.suppliers.PoissonIntSupplierFactory;
import uk.ac.ox.poseidon.core.suppliers.ShiftedIntSupplierFactory;
import uk.ac.ox.poseidon.core.suppliers.temporal.DurationUntilSupplierFactory;
import uk.ac.ox.poseidon.core.suppliers.temporal.NextDayAtTimeSupplierFactory;
import uk.ac.ox.poseidon.core.time.DateTimeAfterFactory;
import uk.ac.ox.poseidon.core.time.TimeFactory;
import uk.ac.ox.poseidon.geography.bathymetry.BathymetricGrid;
import uk.ac.ox.poseidon.geography.bathymetry.BathymetricGridFromGridFileFactory;
import uk.ac.ox.poseidon.geography.bathymetry.adaptors.CellElevationFactory;
import uk.ac.ox.poseidon.geography.distance.DistanceCalculator;
import uk.ac.ox.poseidon.geography.distance.HaversineDistanceCalculatorFactory;
import uk.ac.ox.poseidon.geography.grids.CellSetFromGridFileFactory;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;
import uk.ac.ox.poseidon.geography.grids.ModelGridWithActiveCellsFromGridFile;
import uk.ac.ox.poseidon.geography.paths.DefaultPathFinderFactory;
import uk.ac.ox.poseidon.geography.paths.GridPathFinder;
import uk.ac.ox.poseidon.geography.ports.PortGrid;
import uk.ac.ox.poseidon.geography.ports.PortGridFromFileFactory;
import uk.ac.ox.poseidon.io.ScenarioWriter;
import uk.ac.ox.poseidon.io.tables.CsvTableWriter;
import uk.ac.ox.poseidon.io.tables.CsvTableWriterFactory;
import uk.ac.ox.poseidon.regulations.ForbiddenIfFactory;
import uk.ac.ox.poseidon.regulations.predicates.spatial.ActionCellPredicateFactory;

import java.nio.file.Path;
import java.time.Period;
import java.util.List;
import java.util.function.Predicate;

import static java.time.DayOfWeek.*;
import static uk.ac.ox.poseidon.core.suppliers.ConstantDurationSuppliers.ONE_DAY_DURATION_SUPPLIER;
import static uk.ac.ox.poseidon.core.suppliers.ConstantDurationSuppliers.ONE_HOUR_DURATION_SUPPLIER;
import static uk.ac.ox.poseidon.core.time.PeriodFactory.DAILY;
import static uk.ac.ox.poseidon.core.time.PeriodFactory.MONTHLY;

@Getter
@Setter
public class WesternMedScenario extends ScenarioSupplier {

    private static final double DIFFERENTIAL_PERCENTAGE_TO_MOVE = 0.05;
    private static final double PERCENTAGE_LIMIT_ON_DAILY_MOVEMENT = 0.1;
    private static final double LOGISTIC_GROWTH_RATE = 0.00001;
    private static final String CARRYING_CAPACITY = "10 kg";
    private static final double LEARNING_ALPHA = 1;
    private static final double EXPLORATION_PROBABILITY = 0.2;
    private static final int MEAN_EXPLORATION_RADIUS = 1;
    private static final double CATCH_PROPORTION = 0.1;
    private static final String VESSEL_SPEED = "9.5 kn"; // as per email on 2025-03-18 08:20
    private static final String VESSEL_HOLD_CAPACITY = "1 t";

    private Factory<? extends BiomassGrowthRule> biomassGrowthRule =
        new LogisticGrowthRuleFactory(LOGISTIC_GROWTH_RATE);
    private Factory<? extends BiomassDiffusionRule> biomassDiffusionRule =
        new SmoothBiomassDiffusionRuleFactory(
            DIFFERENTIAL_PERCENTAGE_TO_MOVE,
            PERCENTAGE_LIMIT_ON_DAILY_MOVEMENT
        );
    private PathFactory inputPath = PathFactory.of("data");
    private GlobalScopeFactory<? extends ModelGrid> modelGrid =
        new ModelGridWithActiveCellsFromGridFile(
            new CellSetFromGridFileFactory(
                inputPath.plus("exclusion_grid.asc"),
                0
            )
        );
    private Factory<? extends BathymetricGrid> bathymetricGrid =
        new BathymetricGridFromGridFileFactory(
            inputPath.plus("bathymetry_grid.asc"),
            modelGrid,
            new MaxFactory(),
            false
        );
    private Factory<? extends CarryingCapacityGrid> carryingCapacityGrid =
        new UniformCarryingCapacityGridFactory(
            bathymetricGrid,
            MassFactory.of(CARRYING_CAPACITY)
        );
    private Factory<? extends BiomassAllocator> biomassAllocator =
        new FullBiomassAllocatorFactory(carryingCapacityGrid);
    @SuppressWarnings("MagicNumber")
    private Factory<? extends Regulations> regulations =
        new ForbiddenIfFactory(
            new AnyOfFactory<>(
                new ActionCellPredicateFactory(
                    modelGrid,
                    new AdaptedPredicateFactory<>(
                        new CellElevationFactory(bathymetricGrid),
                        new AboveFactory(-35)
                    )
                ),
                new ActionCellPredicateFactory(
                    modelGrid,
                    new InSetFactory<>(
                        new CellSetFromGridFileFactory(
                            inputPath.plus("french_eez.asc"),
                            1
                        )
                    )
                )
            )
        );
    private Factory<? extends VesselField> vesselField = new VesselFieldFactory(modelGrid);
    private Factory<? extends DistanceCalculator> distance =
        new HaversineDistanceCalculatorFactory(modelGrid);
    private Factory<? extends PortGrid> portGrid =
        new PortGridFromFileFactory(
            bathymetricGrid,
            distance,
            inputPath.plus("ports.csv"),
            "port_id", "port_name", "lon", "lat"
        );
    private Factory<? extends GridPathFinder> pathFinder =
        new DefaultPathFinderFactory(
            bathymetricGrid,
            portGrid,
            distance
        );
    private BehaviourFactory<?> travellingBehaviour =
        new TravellingAlongPathBehaviourFactory(
            pathFinder,
            distance
        );
    private VesselScopeFactory<? extends Predicate<Int2D>> fishingLocationChecker =
        new FishingLocationLegalityCheckerFactory(
            regulations,
            pathFinder,
            distance
        );
    private Factory<? extends List<Species>> species =
        new SpeciesFromFileFactory(
            inputPath.plus("species.csv"),
            "species_id",
            "species_name"
        );
    private Factory<List<BiomassGrid>> biomassGrids =
        new MappedFactory<>(
            species,
            new BiomassGridFactory(
                modelGrid,
                null,
                biomassAllocator
            ),
            "species"
        );
    private Factory<? extends Steppable> dailyProcesses =
        new ScheduledRepeatingFactory<>(
            new DateTimeAfterFactory(
                startingDateTime,
                DAILY
            ),
            DAILY,
            new SteppableSequenceFactory(
                new MappedFactory<>(
                    biomassGrids,
                    new BiomassDiffuserFactory(
                        null,
                        carryingCapacityGrid,
                        biomassDiffusionRule
                    ),
                    "biomassGrid"
                )
            ),
            0
        );
    private Factory<? extends Steppable> monthlyProcesses =
        new ScheduledRepeatingFactory<>(
            new DateTimeAfterFactory(
                startingDateTime,
                MONTHLY
            ),
            MONTHLY,
            new SteppableSequenceFactory(
                new MappedFactory<>(
                    biomassGrids,
                    new BiomassGrowerFactory(
                        null,
                        carryingCapacityGrid,
                        biomassGrowthRule
                    ),
                    "biomassGrid"
                )
            ),
            0
        );
    private Factory<? extends MarketGrid<Biomass, ? extends Market<Biomass>>> marketGrid =
        new BiomassMarketGridPriceFileFactory(
            inputPath.plus("prices.csv"),
            "port_id",
            "species_id",
            "price",
            "currency",
            "measurement_unit",
            portGrid,
            species
        );
    private Factory<? extends Register<MutableOptionValues<Int2D>>> optionValuesRegister =
        new RegisterFactory<>();
    private Factory<? extends CsvTableWriter> catchTableWriter =
        new FinalProcessFactory<>(
            new CsvTableWriterFactory(
                new FishingActionListenerTableFactory(),
                PathFactory.of("outputs", "fishing_actions.csv"),
                true
            )
        );
    private VesselScopeFactory<? extends Hold<Biomass>> hold = new StandardBiomassHoldFactory(
        MassFactory.of(VESSEL_HOLD_CAPACITY),
        MassFactory.of("1 kg"),
        new ProportionalBiomassOvercapacityDiscardingStrategyFactory()
    );
    private VesselScopeFactory<? extends MutableOptionValues<Int2D>> optionValues =
        new RegisteringFactory<>(
            optionValuesRegister,
            new ExponentialMovingAverageOptionValuesFactory<>(LEARNING_ALPHA)
        );
    private Factory<List<Vessel>> vessels =
        new VesselsFromFileFactory(
            inputPath.plus("vessels.csv"),
            "vessel_id",
            "vessel_name",
            "port_id",
            new HomeBehaviourFactory(
                portGrid,
                hold,
                new AllOfFactory<>(
                    new AdaptedVesselPredicateFactory<>(
                        new CurrentTimeFactory(),
                        new TimeIsAfterFactory(new TimeFactory(21, 59, 59))
                    ),
                    new AdaptedVesselPredicateFactory<>(
                        new CurrentDayOfWeekFactory(),
                        new InSetFactory<>(
                            SUNDAY,
                            MONDAY,
                            TUESDAY,
                            WEDNESDAY,
                            THURSDAY
                        )
                    )
                ),
                new ThereAndBackBehaviourFactory(
                    new ChoosingDestinationBehaviourFactory(
                        new EpsilonGreedyDestinationSupplierFactory(
                            EXPLORATION_PROBABILITY,
                            optionValues,
                            new NeighbourhoodGridExplorerFactory(
                                optionValues,
                                fishingLocationChecker,
                                pathFinder,
                                new ShiftedIntSupplierFactory(
                                    new PoissonIntSupplierFactory(MEAN_EXPLORATION_RADIUS),
                                    1
                                )
                            ),
                            new ImitatingPickerFactory<>(
                                optionValues,
                                fishingLocationChecker,
                                new BestOptionsFromFriendsSupplierFactory<>(
                                    5,
                                    optionValuesRegister
                                )
                            ),
                            new TotalBiomassCaughtPerHourDestinationEvaluatorFactory()
                        ),
                        ONE_HOUR_DURATION_SUPPLIER,
                        new WaitingBehaviourFactory(ONE_DAY_DURATION_SUPPLIER)
                    ),
                    new DefaultFishingBehaviourFactory<>(
                        new FixedBiomassProportionGearFactory(
                            CATCH_PROPORTION,
                            ONE_HOUR_DURATION_SUPPLIER
                        ),
                        hold,
                        new CurrentCellFisheableFactory<>(
                            new BiomassGridsFactory(
                                biomassGrids
                            )
                        ),
                        regulations
                    ),
                    travellingBehaviour
                ),
                new WaitingBehaviourFactory(
                    new DurationUntilSupplierFactory(
                        new NextDayAtTimeSupplierFactory(
                            new TimeFactory(22, 0, 0)
                        )
                    )
                ),
                travellingBehaviour,
                new LandingBehaviourFactory<>(marketGrid, hold, ONE_HOUR_DURATION_SUPPLIER)
            ),
            vesselField,
            portGrid,
            SpeedFactory.of(VESSEL_SPEED),
            "EUR"
        );

    public static void main(final String[] args) {
        final Scenario scenario = new WesternMedScenario().get();
        final Path scenarioPath = Path.of("scenario.yaml");
        new ScenarioWriter().write(scenario, scenarioPath);
        new QuickRunner(scenarioPath, Period.ofYears(1)).run();
    }
}
