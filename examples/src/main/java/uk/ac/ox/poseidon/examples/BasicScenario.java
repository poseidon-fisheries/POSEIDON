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
import uk.ac.ox.poseidon.agents.fleets.DefaultFleetFactory;
import uk.ac.ox.poseidon.agents.fleets.Fleet;
import uk.ac.ox.poseidon.agents.registers.Register;
import uk.ac.ox.poseidon.agents.registers.RegisterFactory;
import uk.ac.ox.poseidon.agents.registers.RegisteringFactory;
import uk.ac.ox.poseidon.agents.regulations.FishingLocationLegalityCheckerFactory;
import uk.ac.ox.poseidon.agents.regulations.Regulations;
import uk.ac.ox.poseidon.agents.tables.FishingActionListenerTableFactory;
import uk.ac.ox.poseidon.agents.vessels.RandomHomePortFactory;
import uk.ac.ox.poseidon.agents.vessels.VesselFactory;
import uk.ac.ox.poseidon.agents.vessels.VesselScopeFactory;
import uk.ac.ox.poseidon.agents.vessels.VesselScopeFactoryDecorator;
import uk.ac.ox.poseidon.agents.vessels.gears.FixedBiomassProportionGearFactory;
import uk.ac.ox.poseidon.agents.vessels.hold.Hold;
import uk.ac.ox.poseidon.agents.vessels.hold.ProportionalBiomassOvercapacityDiscardingStrategyFactory;
import uk.ac.ox.poseidon.agents.vessels.hold.StandardBiomassHoldFactory;
import uk.ac.ox.poseidon.biology.biomass.*;
import uk.ac.ox.poseidon.biology.species.Species;
import uk.ac.ox.poseidon.biology.species.SpeciesFactory;
import uk.ac.ox.poseidon.core.*;
import uk.ac.ox.poseidon.core.quantities.MassFactory;
import uk.ac.ox.poseidon.core.quantities.SpeedFactory;
import uk.ac.ox.poseidon.core.schedule.ScheduledRepeatingFactory;
import uk.ac.ox.poseidon.core.schedule.SteppableSequenceFactory;
import uk.ac.ox.poseidon.core.suppliers.PoissonIntSupplierFactory;
import uk.ac.ox.poseidon.core.suppliers.RandomBooleanSupplierFactory;
import uk.ac.ox.poseidon.core.suppliers.ShiftedIntSupplierFactory;
import uk.ac.ox.poseidon.core.time.*;
import uk.ac.ox.poseidon.core.utils.PrefixedIdSupplierFactory;
import uk.ac.ox.poseidon.geography.EnvelopeFactory;
import uk.ac.ox.poseidon.geography.bathymetry.BathymetricGrid;
import uk.ac.ox.poseidon.geography.bathymetry.RoughCoastalBathymetricGridFactory;
import uk.ac.ox.poseidon.geography.distance.DistanceCalculator;
import uk.ac.ox.poseidon.geography.distance.HaversineDistanceCalculatorFactory;
import uk.ac.ox.poseidon.geography.grids.GridExtent;
import uk.ac.ox.poseidon.geography.grids.GridExtentFactory;
import uk.ac.ox.poseidon.geography.paths.DefaultPathFinderFactory;
import uk.ac.ox.poseidon.geography.paths.GridPathFinder;
import uk.ac.ox.poseidon.geography.ports.PortGrid;
import uk.ac.ox.poseidon.geography.ports.RandomLocationsPortGridFactory;
import uk.ac.ox.poseidon.geography.ports.SimplePortFactory;
import uk.ac.ox.poseidon.io.ScenarioWriter;
import uk.ac.ox.poseidon.io.tables.CsvTableWriter;
import uk.ac.ox.poseidon.io.tables.CsvTableWriterFactory;
import uk.ac.ox.poseidon.regulations.ForbiddenIfFactory;
import uk.ac.ox.poseidon.regulations.predicates.logical.AnyOfFactory;
import uk.ac.ox.poseidon.regulations.predicates.spatial.InRectangularAreaPredicateFactory;
import uk.ac.ox.poseidon.regulations.predicates.temporal.BetweenYearlyDatesFactory;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Month;
import java.time.Period;
import java.util.List;
import java.util.function.Predicate;

import static uk.ac.ox.poseidon.core.suppliers.ConstantDurationSuppliers.ONE_DAY_DURATION_SUPPLIER;
import static uk.ac.ox.poseidon.core.suppliers.ConstantDurationSuppliers.ONE_HOUR_DURATION_SUPPLIER;
import static uk.ac.ox.poseidon.core.time.PeriodFactory.DAILY;
import static uk.ac.ox.poseidon.core.time.PeriodFactory.MONTHLY;

@Getter
@Setter
public class BasicScenario extends Scenario {

    private static final double DIFFERENTIAL_PERCENTAGE_TO_MOVE = 0.01;
    private static final double PERCENTAGE_LIMIT_ON_DAILY_MOVEMENT = 0.1;
    private static final double LOGISTIC_GROWTH_RATE = 0.001;
    private static final int GRID_SIZE = 51;
    private static final String CARRYING_CAPACITY = "5 t";
    private static final double LEARNING_ALPHA = 1;
    private static final double READINESS_PROBABILITY = 0.9;
    private static final int NUMBER_OF_PORTS = 1;
    private static final int NUMBER_OF_VESSELS = 500;
    private static final double EXPLORATION_PROBABILITY = 0.2;
    private static final int MEAN_EXPLORATION_RADIUS = 1;
    private static final double CATCH_PROPORTION = 0.1;
    private static final int VESSEL_SPEED = 15;
    private static final String VESSEL_HOLD_CAPACITY = "1 t";

    @SuppressWarnings("MagicNumber")
    private Factory<? extends Regulations> regulations =
        new ForbiddenIfFactory(
            new AnyOfFactory(
                new BetweenYearlyDatesFactory(
                    new MonthDayFactory(Month.MARCH, 1),
                    new MonthDayFactory(Month.MAY, 31)
                ),
                new InRectangularAreaPredicateFactory(
                    new EnvelopeFactory(-3, 3, -3, 3)
                )
            )
        );

    private Factory<? extends Register<MutableOptionValues<Int2D>>> optionValuesRegister =
        new RegisterFactory<>();
    private Factory<? extends Species> speciesA = new SpeciesFactory("A");
    private Factory<? extends Species> speciesB = new SpeciesFactory("B");
    private Factory<? extends BiomassDiffusionRule> biomassDiffusionRule =
        new SmoothBiomassDiffusionRuleFactory(
            DIFFERENTIAL_PERCENTAGE_TO_MOVE,
            PERCENTAGE_LIMIT_ON_DAILY_MOVEMENT
        );
    private Factory<? extends BiomassGrowthRule> biomassGrowthRule =
        new LogisticGrowthRuleFactory(LOGISTIC_GROWTH_RATE);

    private GlobalScopeFactory<? extends GridExtent> gridExtent =
        new GridExtentFactory(
            GRID_SIZE,
            GRID_SIZE,
            -5,
            5,
            -5,
            5
        );

    private Factory<? extends CsvTableWriter> catchTableWriter =
        new FinalProcessFactory<>(
            new CsvTableWriterFactory(
                new FishingActionListenerTableFactory(),
                PathFactory.from("temp", "fishing_actions.csv"),
                true
            )
        );

    private Factory<? extends VesselField> vesselField = new VesselFieldFactory(gridExtent);
    private Factory<? extends DistanceCalculator> distance =
        new HaversineDistanceCalculatorFactory(gridExtent);
    @SuppressWarnings("MagicNumber")
    private Factory<? extends BathymetricGrid> bathymetricGrid =
        new RoughCoastalBathymetricGridFactory(
            gridExtent,
            10,
            1000,
            0.01,
            15,
            -150,
            75,
            0.1
        );
    private Factory<? extends PortGrid> portGrid =
        new RandomLocationsPortGridFactory(
            bathymetricGrid,
            new SimplePortFactory(new PrefixedIdSupplierFactory("Port")),
            NUMBER_OF_PORTS,
            2
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
    private Factory<? extends CarryingCapacityGrid> carryingCapacityGrid =
        new UniformCarryingCapacityGridFactory(
            bathymetricGrid,
            MassFactory.of(CARRYING_CAPACITY)
        );
    private Factory<? extends BiomassAllocator> biomassAllocator =
        new FullBiomassAllocatorFactory(carryingCapacityGrid);
    private Factory<? extends BiomassGrid> biomassGridA =
        new BiomassGridFactory(
            gridExtent,
            speciesA,
            biomassAllocator
        );
    private Factory<? extends BiomassGrid> biomassGridB =
        new BiomassGridFactory(
            gridExtent,
            speciesB,
            biomassAllocator
        );
    private Factory<? extends Steppable> dailyProcesses =
        new ScheduledRepeatingFactory<>(
            new DateTimeAfterFactory(
                startingDateTime,
                DAILY
            ),
            DAILY,
            new SteppableSequenceFactory(
                new BiomassDiffuserFactory(
                    biomassGridA,
                    carryingCapacityGrid,
                    biomassDiffusionRule
                ),
                new BiomassDiffuserFactory(
                    biomassGridB,
                    carryingCapacityGrid,
                    biomassDiffusionRule
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
                new BiomassGrowerFactory(
                    biomassGridA,
                    carryingCapacityGrid,
                    biomassGrowthRule
                ),
                new BiomassGrowerFactory(
                    biomassGridB,
                    carryingCapacityGrid,
                    biomassGrowthRule
                )
            ),
            0
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
    private Factory<? extends Fleet> fleet =
        new DefaultFleetFactory(
            NUMBER_OF_VESSELS,
            new VesselFactory(
                new HomeBehaviourFactory(
                    portGrid,
                    hold,
                    new VesselScopeFactoryDecorator<>(new RandomBooleanSupplierFactory(
                        READINESS_PROBABILITY
                    )),
                    travellingBehaviour,
                    new LandingBehaviourFactory<>(hold, ONE_HOUR_DURATION_SUPPLIER),
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
                                    List.of(biomassGridA, biomassGridB)
                                )
                            ),
                            regulations
                        ),
                        travellingBehaviour
                    ),
                    new WaitingBehaviourFactory(
                        new ExponentiallyDistributedDurationSupplierFactory(
                            new DurationFactory("P10D")
                        )
                    )
                ),
                new PrefixedIdSupplierFactory("Vessel"),
                vesselField,
                new RandomHomePortFactory(portGrid),
                portGrid,
                new SpeedFactory(VESSEL_SPEED, "km/h")
            )
        );

    BasicScenario() {
        super(new DateFactory(LocalDate.now().getYear(), 1, 1));
    }

    public static void main(final String[] args) {
        final BasicScenario scenario = new BasicScenario();
        final Path scenarioPath = Path.of("/home/nicolas/Desktop/scenario.yaml");
        new ScenarioWriter().write(
            scenario,
            scenarioPath
        );
        new QuickRunner(scenarioPath, Period.ofYears(1)).run();
    }
}
