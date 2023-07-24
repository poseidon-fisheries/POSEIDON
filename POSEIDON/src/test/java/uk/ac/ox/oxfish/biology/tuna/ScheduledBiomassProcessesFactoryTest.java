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

package uk.ac.ox.oxfish.biology.tuna;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.mapmakers.FromFileMapInitializer;
import uk.ac.ox.oxfish.geography.mapmakers.MapInitializer;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.InputPath;
import uk.ac.ox.oxfish.utility.parameters.IntegerParameter;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.mockito.Mockito.mock;
import static uk.ac.ox.oxfish.model.scenario.EpoScenario.DEFAULT_MAP_EXTENT_FACTORY;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.EPSILON;

public class ScheduledBiomassProcessesFactoryTest {

    @Test
    public void testBuildBiomassGrids() {

        final InputPath inputFolder = InputPath.of("inputs", "epo_inputs");

        final BiomassReallocatorFactory biomassReallocatorFactory = new BiomassReallocatorFactory(
            inputFolder.path("biomass", "biomass_distributions.csv"),
            new IntegerParameter(365),
            DEFAULT_MAP_EXTENT_FACTORY
        );

        final Path depthFile = Paths.get("inputs", "epo_inputs", "depth.csv");
        final MapInitializer mapInitializer =
            new FromFileMapInitializer(depthFile, 101, 0.5, true, true);
        final NauticalMap nauticalMap = mapInitializer.makeMap(null, null, null);
        final BiomassReallocator biomassReallocator =
            biomassReallocatorFactory.apply(mock(FishState.class));
        final AllocationGrids<?> allocationGrids =
            biomassReallocator.getAllocationGrids();
        Assertions.assertEquals(12, allocationGrids.size());
        allocationGrids.values().forEach(gridsPerSpecies -> {
            Assertions.assertEquals(3, gridsPerSpecies.size());
            gridsPerSpecies.values().forEach(grid -> {
                Assertions.assertEquals(100, grid.getHeight());
                Assertions.assertEquals(101, grid.getWidth());
                Assertions.assertEquals(1.0, Arrays.stream(grid.field).flatMapToDouble(Arrays::stream).sum(), EPSILON);
            });
        });
    }
}