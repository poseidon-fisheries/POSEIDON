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
import static uk.ac.ox.oxfish.model.scenario.EpoScenario.TESTS_INPUT_PATH;
import static uk.ac.ox.oxfish.model.scenario.TestableScenario.startTestableScenario;

import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import junit.framework.TestCase;
import sim.field.grid.DoubleGrid2D;
import sim.util.Int2D;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.biology.tuna.AbundanceRestorer;
import uk.ac.ox.oxfish.biology.tuna.Reallocator;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.PurseSeineGearFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.AbundanceFad;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fishing.PurseSeinerFishingStrategyFactory;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.utility.operators.CompressedExponentialFunctionFactory;


public class EpoAbundanceScenarioTest extends TestCase {

    public void testRunOneYearWithoutCrashing() {
        final FishState fishState = startTestableScenario(EpoAbundanceScenario.class);
        do {
            fishState.schedule.step(fishState);
        } while (fishState.getYear() < 1);
    }

    public void testSaveAndLoadLogisticYaml() {
        ScenarioTestUtils.testSaveAndLoadYaml(
            TESTS_INPUT_PATH,
            "epo_abundance_logistic.yaml",
            EpoAbundanceScenario.class
        );
    }

    public void testSaveAndLoadExponentialYaml() {
        ScenarioTestUtils.testSaveAndLoadYaml(
            TESTS_INPUT_PATH,
            "epo_abundance_exponential.yaml",
            EpoAbundanceScenario.class,
            EpoAbundanceScenarioTest::useCompressedExponentials
        );
    }

    private static EpoAbundanceScenario useCompressedExponentials(final EpoAbundanceScenario scenario) {

        final PurseSeineGearFactory<AbundanceLocalBiology, AbundanceFad> gearFactory =
            scenario.getPurseSeineGearFactory();
        final PurseSeinerFishingStrategyFactory<AbundanceLocalBiology, AbundanceFad>
            strategyFactory = (PurseSeinerFishingStrategyFactory<AbundanceLocalBiology, AbundanceFad>) scenario.getFishingStrategyFactory();

        gearFactory.setPctHoldSpaceLeftModulationFunction(
            new CompressedExponentialFunctionFactory(0.5)
        );
        gearFactory.setPctSetsRemainingModulationFunction(
            new CompressedExponentialFunctionFactory(0.5)
        );
        gearFactory.setNumFadsInStockModulationFunction(
            new CompressedExponentialFunctionFactory(0.5)
        );
        gearFactory.setFadDeploymentPctActiveFadsLimitModulationFunction(
            new CompressedExponentialFunctionFactory(0.5)
        );
        gearFactory.setPctTravelTimeLeftModulationFunction(
            new CompressedExponentialFunctionFactory(0.5)
        );
        gearFactory.setOpportunisticFadSetTimeSinceLastVisitModulationFunction(
            new CompressedExponentialFunctionFactory(0.5)
        );
        gearFactory.setNonAssociatedSetTimeSinceLastVisitModulationFunction(
            new CompressedExponentialFunctionFactory(0.5)
        );
        gearFactory.setDolphinSetTimeSinceLastVisitModulationFunction(
            new CompressedExponentialFunctionFactory(0.5)
        );

        strategyFactory.setDolphinSetActionValueFunction(
            new CompressedExponentialFunctionFactory(0.5)
        );
        strategyFactory.setFadDeploymentActionValueFunction(
            new CompressedExponentialFunctionFactory(0.5)
        );
        strategyFactory.setFadSetActionValueFunction(
            new CompressedExponentialFunctionFactory(0.5)
        );
        strategyFactory.setNonAssociatedSetActionValueFunction(
            new CompressedExponentialFunctionFactory(0.5)
        );
        strategyFactory.setOpportunisticFadSetActionValueFunction(
            new CompressedExponentialFunctionFactory(0.5)
        );
        strategyFactory.setDolphinSetGeneratorFunction(
            new CompressedExponentialFunctionFactory(0.5)
        );
        strategyFactory.setNonAssociatedSetGeneratorFunction(
            new CompressedExponentialFunctionFactory(0.5)
        );
        strategyFactory.setSearchActionValueFunction(
            new CompressedExponentialFunctionFactory(0.5)
        );
        
        return scenario;
    }

    /**
     * We need to make sure that all non-zero biomass grid cells in all our allocation grids match
     * an actual SeaTile with the right biology (BiomassLocalBiology or AbundanceLocalBiology)
     * Otherwise, even if the grid's values sum up to 1.0, some biomass will be lost during the
     * reallocation process.
     */
    public void testAllNonZeroGridCellsMapRightBiologySeaTiles() {

        final FishState fishState = startTestableScenario(EpoAbundanceScenario.class);
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