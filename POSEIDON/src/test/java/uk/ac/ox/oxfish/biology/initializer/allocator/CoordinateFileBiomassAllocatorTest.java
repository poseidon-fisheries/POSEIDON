/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

package uk.ac.ox.oxfish.biology.initializer.allocator;

import ec.util.MersenneTwisterFast;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.mapmakers.FromFileMapInitializer;
import uk.ac.ox.oxfish.model.FishState;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.mockito.Mockito.mock;
import static uk.ac.ox.oxfish.geography.mapmakers.FromFileMapInitializerFactory.DEFAULT_MAP_PADDING_IN_DEGREES;

public class CoordinateFileBiomassAllocatorTest {

    @Test
    public void coordinateFileBiomass() {
        //this example is from FromFileMapInitializerTest

        Path path = Paths.get("inputs", "tests", "map.csv");
        final FromFileMapInitializer initializer = new FromFileMapInitializer(
            path, 2, DEFAULT_MAP_PADDING_IN_DEGREES, true, false
        );
        final NauticalMap map = initializer.makeMap(
            new MersenneTwisterFast(),
            new GlobalBiology(mock(Species.class)),
            mock(FishState.class)
        );
        Assertions.assertEquals(map.getHeight(), 2);
        Assertions.assertEquals(map.getWidth(), 2);

        //let's just read the depth flipped upside down for abundance
        path = Paths.get("inputs", "tests", "flipped_map.csv");
        final CoordinateFileBiomassAllocator allocator = new CoordinateFileBiomassAllocator(
            path, true
        );
        Assertions.assertEquals(6, allocator.allocate(map.getSeaTile(0, 0), map, new MersenneTwisterFast()), .0001);
        Assertions.assertEquals(6, allocator.allocate(map.getSeaTile(0, 1), map, new MersenneTwisterFast()), .0001);
        //this ought to be neutralized by the initializer
        Assertions.assertEquals(-10, allocator.allocate(map.getSeaTile(1, 1), map, new MersenneTwisterFast()), .0001);
        Assertions.assertEquals(-10, allocator.allocate(map.getSeaTile(1, 1), map, new MersenneTwisterFast()), .0001);


    }

}
