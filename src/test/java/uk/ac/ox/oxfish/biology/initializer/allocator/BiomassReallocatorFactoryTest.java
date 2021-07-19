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

package uk.ac.ox.oxfish.biology.initializer.allocator;

import junit.framework.TestCase;
import sim.field.grid.DoubleGrid2D;
import uk.ac.ox.oxfish.geography.MapExtent;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.mapmakers.FromFileMapInitializer;
import uk.ac.ox.oxfish.geography.mapmakers.MapInitializer;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.TunaScenario;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.EPSILON;

public class BiomassReallocatorFactoryTest extends TestCase {

    public void testBuildBiomassGrids() {
        final BiomassReallocatorFactory biomassReallocatorFactory = new BiomassReallocatorFactory();
        final Path depthFile = TunaScenario.input("depth.csv");
        final MapInitializer mapInitializer = new FromFileMapInitializer(depthFile, 101, 0.5, true, true);
        final NauticalMap nauticalMap = mapInitializer.makeMap(null, null, null);
        biomassReallocatorFactory.setMapExtent(new MapExtent(nauticalMap));
        final BiomassReallocator biomassReallocator = biomassReallocatorFactory.apply(mock(FishState.class));
        assertEquals(365, biomassReallocator.getPeriod());
        assertEquals(12, biomassReallocator.getAllocationGrids().size());
        final Collection<Map<String, DoubleGrid2D>> biomassGrids = biomassReallocator.getAllocationGrids().values();
        biomassGrids.forEach(gridsPerSpecies -> {
            assertEquals(3, gridsPerSpecies.size());
            gridsPerSpecies.values().forEach(grid -> {
                assertEquals(100, grid.getHeight());
                assertEquals(101, grid.getWidth());
                assertEquals(1.0, Arrays.stream(grid.field).flatMapToDouble(Arrays::stream).sum(), EPSILON);
            });
        });
    }
}