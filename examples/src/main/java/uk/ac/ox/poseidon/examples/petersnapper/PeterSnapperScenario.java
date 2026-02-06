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

import uk.ac.ox.poseidon.biology.biomass.CarryingCapacityGridFactory;
import uk.ac.ox.poseidon.core.Scenario;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.aggregators.MeanFactory;
import uk.ac.ox.poseidon.core.quantities.MassFactory;
import uk.ac.ox.poseidon.geography.bathymetry.BathymetricGridFromElevationTableFactory;
import uk.ac.ox.poseidon.geography.grids.ModelGridFromLonLatTableFactory;
import uk.ac.ox.poseidon.geography.utils.ElevationTableFactory;
import uk.ac.ox.poseidon.io.paths.PathFactory;
import uk.ac.ox.poseidon.io.tables.CsvTableFactory;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.function.Supplier;

import static si.uom.NonSI.TONNE;

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
            CarryingCapacityGridFactory.ofTotalCapacity(
                modelGrid,
                bathymetricGrid,
                MassFactory.of(500_000, TONNE)
            );

        return builder
            .startingDateTime(LocalDate.now())
            .component("bathymetricGrid", bathymetricGrid)
            .component("carryingCapacityGrid", carryingCapacityGrid)
            .build();
    }
}
