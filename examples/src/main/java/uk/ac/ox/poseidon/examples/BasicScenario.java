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
import uk.ac.ox.poseidon.agents.behaviours.BackToInitialBehaviourFactory;
import uk.ac.ox.poseidon.agents.behaviours.WaitBehaviourFactory;
import uk.ac.ox.poseidon.agents.behaviours.choices.BestOptionsRegisterFactory;
import uk.ac.ox.poseidon.agents.behaviours.choices.ExponentialMovingAverageOptionValuesFactory;
import uk.ac.ox.poseidon.agents.behaviours.choices.OptionValues;
import uk.ac.ox.poseidon.agents.behaviours.destination.*;
import uk.ac.ox.poseidon.agents.behaviours.fishing.DefaultFishingBehaviourFactory;
import uk.ac.ox.poseidon.agents.behaviours.travel.TravelAlongPathBehaviourFactory;
import uk.ac.ox.poseidon.agents.fields.VesselField;
import uk.ac.ox.poseidon.agents.fields.VesselFieldFactory;
import uk.ac.ox.poseidon.agents.fisheables.CurrentCellFisheableFactory;
import uk.ac.ox.poseidon.agents.fleets.DefaultFleetFactory;
import uk.ac.ox.poseidon.agents.fleets.Fleet;
import uk.ac.ox.poseidon.agents.registers.MutableRegister;
import uk.ac.ox.poseidon.agents.registers.MutableRegisterFactory;
import uk.ac.ox.poseidon.agents.registers.RegisteringFactory;
import uk.ac.ox.poseidon.agents.vessels.RandomHomePortFactory;
import uk.ac.ox.poseidon.agents.vessels.VesselFactory;
import uk.ac.ox.poseidon.agents.vessels.VesselScopeFactory;
import uk.ac.ox.poseidon.agents.vessels.gears.FixedBiomassProportionGearFactory;
import uk.ac.ox.poseidon.agents.vessels.hold.VoidHoldFactory;
import uk.ac.ox.poseidon.biology.biomass.*;
import uk.ac.ox.poseidon.biology.species.Species;
import uk.ac.ox.poseidon.biology.species.SpeciesFactory;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.GlobalScopeFactory;
import uk.ac.ox.poseidon.core.Scenario;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.schedule.ScheduledRepeatingFactory;
import uk.ac.ox.poseidon.core.schedule.SteppableSequenceFactory;
import uk.ac.ox.poseidon.core.suppliers.PoissonIntSupplierFactory;
import uk.ac.ox.poseidon.core.suppliers.ShiftedIntSupplierFactory;
import uk.ac.ox.poseidon.core.time.*;
import uk.ac.ox.poseidon.core.utils.PrefixedIdSupplierFactory;
import uk.ac.ox.poseidon.geography.bathymetry.BathymetricGrid;
import uk.ac.ox.poseidon.geography.bathymetry.RoughCoastalBathymetricGridFactory;
import uk.ac.ox.poseidon.geography.distance.Distance;
import uk.ac.ox.poseidon.geography.distance.EquirectangularDistanceFactory;
import uk.ac.ox.poseidon.geography.grids.GridExtent;
import uk.ac.ox.poseidon.geography.grids.GridExtentFactory;
import uk.ac.ox.poseidon.geography.paths.DefaultPathFinderFactory;
import uk.ac.ox.poseidon.geography.paths.GridPathFinder;
import uk.ac.ox.poseidon.geography.ports.PortGrid;
import uk.ac.ox.poseidon.geography.ports.RandomLocationsPortGridFactory;
import uk.ac.ox.poseidon.geography.ports.SimplePortFactory;
import uk.ac.ox.poseidon.io.ScenarioWriter;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

@SuppressWarnings("MagicNumber")
@Getter
@Setter
public class BasicScenario extends Scenario {

    private Factory<? extends MutableRegister<OptionValues<Int2D>>> optionValuesRegister =
        new MutableRegisterFactory<>();
    private Factory<? extends Species> speciesA = new SpeciesFactory("A");
    private Factory<? extends Species> speciesB = new SpeciesFactory("B");
    private Factory<? extends BiomassDiffusionRule> biomassDiffusionRule =
        new SmoothBiomassDiffusionRuleFactory(0.001, 0.01);
    private Factory<? extends BiomassGrowthRule> biomassGrowthRule =
        new LogisticGrowthRuleFactory(0.7);

    private GlobalScopeFactory<? extends GridExtent> gridExtent =
        new GridExtentFactory(
            51,
            51,
            -5,
            5,
            -5,
            5
        );
    private Factory<? extends VesselField> vesselField = new VesselFieldFactory(gridExtent);
    private Factory<? extends Distance> distance = new EquirectangularDistanceFactory(gridExtent);
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
            3,
            2
        );

    private Factory<? extends GridPathFinder> pathFinder =
        new DefaultPathFinderFactory(
            bathymetricGrid,
            portGrid,
            distance
        );
    private Factory<? extends CarryingCapacityGrid> carryingCapacityGrid =
        new UniformCarryingCapacityGridFactory(
            bathymetricGrid,
            5000
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
                new PeriodFactory(0, 0, 1)
            ),
            new PeriodFactory(0, 0, 1),
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
                new PeriodFactory(0, 1, 0)
            ),
            new PeriodFactory(0, 1, 0),
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
    private VesselScopeFactory<? extends OptionValues<Int2D>> optionValues =
        new RegisteringFactory<>(
            optionValuesRegister,
            new ExponentialMovingAverageOptionValuesFactory<>(0.5)
        );
    private Factory<? extends Fleet> fleet =
        new DefaultFleetFactory(
            500,
            new VesselFactory(
                new WaitBehaviourFactory(
                    new ExponentiallyDistributedDurationSupplierFactory(
                        new DurationFactory(10, 0, 0, 0)
                    ),
                    new ChooseDestinationBehaviourFactory(
                        new EpsilonGreedyDestinationSupplierFactory(
                            0.2,
                            optionValues,
                            new NeighbourhoodGridExplorerFactory(
                                optionValues,
                                pathFinder,
                                new ShiftedIntSupplierFactory(
                                    new PoissonIntSupplierFactory(5),
                                    1
                                )
                            ),
                            new ImitatingCellPickerFactory(
                                optionValues,
                                new BestOptionsRegisterFactory<>(
                                    optionValuesRegister
                                ),
                                pathFinder
                            ),
                            new TotalBiomassCaughtPerHourDestinationEvaluatorFactory()
                        ),
                        new TravelAlongPathBehaviourFactory(
                            new DefaultFishingBehaviourFactory<>(
                                new FixedBiomassProportionGearFactory(
                                    0.05,
                                    new DurationFactory(0, 1, 0, 0)
                                ),
                                new VoidHoldFactory<>(),
                                new CurrentCellFisheableFactory<>(
                                    new BiomassGridsFactory(
                                        List.of(biomassGridA, biomassGridB)
                                    )
                                ),
                                new ChooseDestinationBehaviourFactory(
                                    new HomePortDestinationSupplierFactory(portGrid),
                                    new TravelAlongPathBehaviourFactory(
                                        new BackToInitialBehaviourFactory(),
                                        pathFinder,
                                        distance
                                    )
                                )
                            ),
                            pathFinder,
                            distance
                        )
                    )
                ),
                new PrefixedIdSupplierFactory("Vessel"),
                vesselField,
                new RandomHomePortFactory(portGrid),
                portGrid,
                15
            )
        );

    BasicScenario() {
        super(new DateFactory(LocalDate.now().getYear(), 1, 1));
    }

    public static void main(final String[] args) {
        final BasicScenario scenario = new BasicScenario();
        new ScenarioWriter().write(
            scenario,
            Path.of("/home/nicolas/Desktop/scenario.yaml")
        );
        final Simulation simulation = scenario.newSimulation();
        simulation.start();
        while (
            simulation
                .getTemporalSchedule()
                .getDateTime()
                .isBefore(LocalDate.of(LocalDate.now().getYear() + 10, 1, 1).atStartOfDay())
        ) {
            simulation.schedule.step(simulation);
        }
    }
}
