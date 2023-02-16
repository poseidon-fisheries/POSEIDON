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

import junit.framework.TestCase;
import uk.ac.ox.oxfish.biology.SpeciesCodesFromFileFactory;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.mapmakers.FromFileMapInitializer;
import uk.ac.ox.oxfish.geography.mapmakers.MapInitializer;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.EpoBiomassScenario;
import uk.ac.ox.oxfish.model.scenario.InputFile;
import uk.ac.ox.oxfish.model.scenario.RootFolder;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.mockito.Mockito.mock;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.EPSILON;

public class ScheduledBiomassProcessesFactoryTest extends TestCase {

    public void testBuildBiomassGrids() {
        final BiomassReallocatorFactory biomassReallocatorFactory = new BiomassReallocatorFactory(
            EpoBiomassScenario.INPUT_PATH.resolve("biomass").resolve("biomass_distributions.csv"),
            365
        );

        biomassReallocatorFactory.setSpeciesCodes(
            new SpeciesCodesFromFileFactory(
                new InputFile(
                    new RootFolder(EpoBiomassScenario.INPUT_PATH),
                    Paths.get("species_codes.csv")
                )
            ).get()
        );

        final Path depthFile = EpoBiomassScenario.INPUT_PATH.resolve("depth.csv");
        final MapInitializer mapInitializer =
            new FromFileMapInitializer(depthFile, 101, 0.5, true, true);
        final NauticalMap nauticalMap = mapInitializer.makeMap(null, null, null);
        biomassReallocatorFactory.setMapExtent(nauticalMap.getMapExtent());
        final BiomassReallocator biomassReallocator =
            biomassReallocatorFactory.apply(mock(FishState.class));
        final AllocationGrids<String> allocationGrids =
            biomassReallocator.getAllocationGrids();
        assertEquals(12, allocationGrids.size());
        allocationGrids.values().forEach(gridsPerSpecies -> {
            assertEquals(3, gridsPerSpecies.size());
            gridsPerSpecies.values().forEach(grid -> {
                assertEquals(100, grid.getHeight());
                assertEquals(101, grid.getWidth());
                assertEquals(
                    1.0,
                    Arrays.stream(grid.field).flatMapToDouble(Arrays::stream).sum(),
                    EPSILON
                );
            });
        });
    }
}