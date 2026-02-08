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

import uk.ac.ox.poseidon.agents.catches.CatchCategory;
import uk.ac.ox.poseidon.agents.market.MarketGridFactory;
import uk.ac.ox.poseidon.agents.market.OneBiomassMarketPerPortFactory;
import uk.ac.ox.poseidon.agents.market.PriceEntryFactory;
import uk.ac.ox.poseidon.agents.market.PriceFactory;
import uk.ac.ox.poseidon.biology.allocators.ProportionOfCarryingCapacityAllocatorFactory;
import uk.ac.ox.poseidon.biology.biomass.*;
import uk.ac.ox.poseidon.biology.species.SpeciesFactory;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.Scenario;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.aggregators.MeanFactory;
import uk.ac.ox.poseidon.core.predicates.logical.AllOfFactory;
import uk.ac.ox.poseidon.core.quantities.KilogramsFactory;
import uk.ac.ox.poseidon.core.quantities.MassFactory;
import uk.ac.ox.poseidon.core.schedule.ScheduledRepeatingFactory;
import uk.ac.ox.poseidon.core.schedule.SteppableSequenceFactory;
import uk.ac.ox.poseidon.core.time.DateTimeFactory;
import uk.ac.ox.poseidon.core.utils.ListFactory;
import uk.ac.ox.poseidon.core.utils.PairFactory;
import uk.ac.ox.poseidon.geography.CoordinateFactory;
import uk.ac.ox.poseidon.geography.allocators.FilteredAllocatorFactory;
import uk.ac.ox.poseidon.geography.allocators.SupplierAllocatorFactory;
import uk.ac.ox.poseidon.geography.bathymetry.BathymetricGridFromElevationTableFactory;
import uk.ac.ox.poseidon.geography.distance.HaversineDistanceCalculatorFactory;
import uk.ac.ox.poseidon.geography.grids.ModelGridFromLonLatTableFactory;
import uk.ac.ox.poseidon.geography.grids.NormalisedDoubleGridFromAllocatorFactory;
import uk.ac.ox.poseidon.geography.ports.PortFactory;
import uk.ac.ox.poseidon.geography.ports.PortGridFactory;
import uk.ac.ox.poseidon.geography.predicates.IsActiveWaterCellFactory;
import uk.ac.ox.poseidon.geography.predicates.IsInDepthRangeFactory;
import uk.ac.ox.poseidon.geography.utils.ElevationTableFactory;
import uk.ac.ox.poseidon.io.paths.PathFactory;
import uk.ac.ox.poseidon.io.tables.CsvTableFactory;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Supplier;

import static tech.units.indriya.unit.Units.KILOGRAM;
import static uk.ac.ox.poseidon.core.suppliers.SupplierFactories.constantDouble;
import static uk.ac.ox.poseidon.core.suppliers.SupplierFactories.randomDouble;
import static uk.ac.ox.poseidon.core.time.DurationFactory.ONE_DAY;

public class PeterSnapperScenario implements Supplier<Scenario> {

    public static void main(final String[] args) {
        final Scenario scenario = new PeterSnapperScenario().get();
        final Simulation simulation = scenario.startNewSimulation();
        System.out.println(simulation.getTemporalSchedule().getDateTime());
    }

    private static final Path INPUT_PATH =
        Path.of("inputs", "peter_snapper");

    @Override
    public Scenario get() {
        final Scenario.ScenarioBuilder builder = Scenario.builder();

        final var inputPath = PathFactory.of(INPUT_PATH);
        final LocalDateTime startingDateTime = LocalDate.now().atStartOfDay();

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
                        new AllOfFactory<>(
                            new IsActiveWaterCellFactory<>(bathymetricGrid),
                            new IsInDepthRangeFactory<>(
                                bathymetricGrid,
                                30,
                                800
                            )
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
                DateTimeFactory.of(startingDateTime),
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

        final var distance =
            new HaversineDistanceCalculatorFactory<>(modelGrid);
        final var portGrid =
            new PortGridFactory<>(
                new ListFactory<>(
                    List.of(
                        new PairFactory<>(
                            new PortFactory("P1", "Benoa"),
                            new CoordinateFactory(115.238843, -8.799605)
                        ),
                        new PairFactory<>(
                            new PortFactory("P2", "Kupang"),
                            new CoordinateFactory(123.586249, -10.148044)
                        )
                    )
                ),
                bathymetricGrid,
                distance
            );

        final var marketGrid = new MarketGridFactory<>(
            portGrid,
            new OneBiomassMarketPerPortFactory(
                portGrid,
                new ListFactory<>(
                    List.of(
                        new PriceEntryFactory<>(
                            Factory.of(CatchCategory.UNCATEGORISED),
                            species,
                            new PriceFactory(40000.0, "IDR", "kg")
                        )
                    )
                )
            )
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
            .build();
    }
}
