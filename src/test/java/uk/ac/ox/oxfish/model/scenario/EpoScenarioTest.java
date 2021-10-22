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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import junit.framework.TestCase;
import sim.field.grid.DoubleGrid2D;
import sim.util.Int2D;
import uk.ac.ox.oxfish.biology.tuna.AbundanceRestorer;
import uk.ac.ox.oxfish.biology.tuna.Aggregator;
import uk.ac.ox.oxfish.biology.tuna.Reallocator;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.currents.CurrentPattern;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;


public class EpoScenarioTest extends TestCase {

    /**
     * We need to make sure that all non-zero biomass grid cells in all our allocation grids match
     * an actual SeaTile with the right biology (BiomassLocalBiology or AbundanceLocalBiology)
     * Otherwise, even if the grid's values sum up to 1.0, some biomass will be lost during the
     * reallocation process.
     */
    public void testAllNonZeroGridCellsMapRightBiologySeaTiles() {

        final FishState fishState = new FishState();
        final EpoScenario scenario = new EpoScenario();
        scenario.getFadMapFactory().setCurrentFiles(ImmutableMap.of());
        scenario.setVesselsFilePath(EpoScenario.INPUT_PATH.resolve("dummy_boats.csv"));

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
        final Aggregator<?> aggregator = abundanceRestorer.getAggregator();
        final NauticalMap nauticalMap = fishState.getMap();

        final Set<Int2D> rightBiologySeaTiles =
            nauticalMap.getAllSeaTilesExcludingLandAsList()
                .stream()
                .filter(seaTile ->
                    aggregator.getLocalBiologiesExtractor()
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