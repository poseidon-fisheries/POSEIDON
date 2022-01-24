/*
 * POSEIDON, an agent-based model of fisheries
 * Copyright (C) 2021 CoHESyS Lab cohesys.lab@gmail.com
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.model.scenario;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.util.stream.IntStream.range;
import static uk.ac.ox.oxfish.model.scenario.EpoAbundanceScenario.INPUT_PATH;

import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Sets;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;
import junit.framework.TestCase;
import sim.field.grid.DoubleGrid2D;
import sim.util.Int2D;
import uk.ac.ox.oxfish.biology.tuna.AbundanceRestorer;
import uk.ac.ox.oxfish.biology.tuna.Reallocator;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;


public class EpoAbundanceScenarioTest extends TestCase {

    public void testRunOneYearWithoutCrashing() {
        final EpoAbundanceScenario scenario = new EpoAbundanceScenario();
        scenario.useDummyData(INPUT_PATH.resolve("test"));
        final FishState fishState = new FishState();
        fishState.setScenario(scenario);

        fishState.start();
        do {
            fishState.schedule.step(fishState);
        } while (fishState.getYear() < 1);

    }

    public void testSaveAndLoadYaml() {

        // Dump the scenario to YAML
        final File scenarioFile = Paths.get("inputs", "tests", "epo.yaml").toFile();
        try {
            final Scenario scenario = new EpoAbundanceScenario();
            new FishYAML().dump(scenario, new FileWriter(scenarioFile));
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }

        // Try to read it back and start it
        try (final FileReader fileReader = new FileReader(scenarioFile)) {
            final FishYAML fishYAML = new FishYAML();
            final EpoAbundanceScenario epoAbundanceScenario =
                fishYAML.loadAs(fileReader, EpoAbundanceScenario.class);
            epoAbundanceScenario.useDummyData(Paths.get("inputs", "epo", "test"));
            final FishState fishState = new FishState();
            fishState.setScenario(epoAbundanceScenario);
            fishState.start();
        } catch (final FileNotFoundException e) {
            throw new IllegalArgumentException("Can't find scenario file: " + scenarioFile, e);
        } catch (final IOException e) {
            throw new IllegalStateException("Error while reading file: " + scenarioFile, e);
        }

    }

    /**
     * We need to make sure that all non-zero biomass grid cells in all our allocation grids match
     * an actual SeaTile with the right biology (BiomassLocalBiology or AbundanceLocalBiology)
     * Otherwise, even if the grid's values sum up to 1.0, some biomass will be lost during the
     * reallocation process.
     */
    public void testAllNonZeroGridCellsMapRightBiologySeaTiles() {

        final FishState fishState = new FishState();
        final EpoAbundanceScenario scenario = new EpoAbundanceScenario();
        scenario.useDummyData(INPUT_PATH.resolve("test"));

        fishState.setScenario(scenario);
        fishState.start();

        final List<Startable> startables = fishState.viewStartables();

        @SuppressWarnings("OptionalGetWithoutIsPresent") final AbundanceRestorer abundanceRestorer =
            startables.stream()
                .filter(startable -> startable instanceof AbundanceRestorer)
                .map(startable -> (AbundanceRestorer) startable)
                .findAny()
                .get();

        final Reallocator<?, ?> reallocator = abundanceRestorer.getReallocator();
        final NauticalMap nauticalMap = fishState.getMap();

        final Set<Int2D> rightBiologySeaTiles =
            nauticalMap.getAllSeaTilesExcludingLandAsList()
                .stream()
                .filter(seaTile ->
                    abundanceRestorer.getExtractor()
                        .getLocalBiologyClass()
                        .isInstance(seaTile.getBiology())
                )
                .map(seaTile -> new Int2D(seaTile.getGridX(), seaTile.getGridY()))
                .collect(toImmutableSet());

        final ImmutableSortedMap<Integer, ? extends Map<?, DoubleGrid2D>> grids =
            reallocator.getAllocationGrids().getGrids();
        assertTrue(
            grids.entrySet().stream().allMatch(entry1 ->
                entry1.getValue().entrySet().stream().allMatch(entry2 -> {
                    final DoubleGrid2D grid = entry2.getValue();
                    final Set<Int2D> nonZeroGridCells =
                        range(0, grid.field.length).boxed()
                            .flatMap(x ->
                                range(0, grid.field[x].length)
                                    .filter(y -> grid.get(x, y) > 0)
                                    .boxed()
                                    .map(y -> new Int2D(x, y))
                            )
                            .collect(toImmutableSet());
                    return Sets.difference(nonZeroGridCells, rightBiologySeaTiles).isEmpty();
                })
            )
        );
    }

}