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

package uk.ac.ox.oxfish.geography.mapmakers;

import com.google.common.collect.HashBasedTable;
import com.vividsolutions.jts.geom.Coordinate;
import ec.util.MersenneTwisterFast;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.model.FishState;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.mockito.Mockito.mock;
import static uk.ac.ox.oxfish.geography.mapmakers.FromFileMapInitializerFactory.DEFAULT_MAP_PADDING_IN_DEGREES;

public class FromFileMapInitializerTest {


    @Test
    public void readMapCorrectly() {
        final Path path = Paths.get("inputs", "tests", "map.csv");
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
        //notice that
        // the coordinates flip (that's because Y is reversed in most computer coordinates)
        // the cut is somewhere at 5.5 because numbers go from 1 to 10 in the input
        Assertions.assertEquals(map.getSeaTile(0, 1), map.getSeaTile(new Coordinate(1, 1)));
        Assertions.assertEquals(map.getSeaTile(0, 1), map.getSeaTile(new Coordinate(3, 3)));
        Assertions.assertEquals(map.getSeaTile(0, 1), map.getSeaTile(new Coordinate(3, 1.0001)));
        Assertions.assertEquals(map.getSeaTile(0, 1), map.getSeaTile(new Coordinate(1.0001, 4)));
        Assertions.assertEquals(map.getSeaTile(0, 1), map.getSeaTile(new Coordinate(5, 5)));


        Assertions.assertEquals(map.getSeaTile(1, 1), map.getSeaTile(new Coordinate(5.51, 1)));
        Assertions.assertEquals(map.getSeaTile(1, 1), map.getSeaTile(new Coordinate(8, 3)));
        Assertions.assertEquals(map.getSeaTile(1, 1), map.getSeaTile(new Coordinate(8, 1)));
        Assertions.assertEquals(map.getSeaTile(1, 1), map.getSeaTile(new Coordinate(5.51, 4)));
        Assertions.assertEquals(map.getSeaTile(1, 1), map.getSeaTile(new Coordinate(9, 4)));
        Assertions.assertEquals(map.getSeaTile(1, 1), map.getSeaTile(new Coordinate(10, 1)));

        //because cell range is 1 to 5, gridX should have -6 as depth (because the input goes negative only form 1 to 4)
        Assertions.assertEquals(map.getSeaTile(1, 1).getAltitude(), +10, .0001);
        Assertions.assertEquals(map.getSeaTile(1, 0).getAltitude(), +10, .0001);
        Assertions.assertEquals(map.getSeaTile(0, 1).getAltitude(), -6, .0001);
        Assertions.assertEquals(map.getSeaTile(0, 0).getAltitude(), -6, .0001);

    }


    @Test
    public void overridesChangeAltitude() {
        final Path path = Paths.get("inputs", "tests", "map.csv");
        final HashBasedTable<Integer, Integer, Double> override = HashBasedTable.create(1, 1);
        override.put(1, 1, -1234d);
        final FromFileMapInitializer initializer = new FromFileMapInitializer(
            path, 2, DEFAULT_MAP_PADDING_IN_DEGREES, true, false,
            override
        );
        final NauticalMap map = initializer.makeMap(
            new MersenneTwisterFast(),
            new GlobalBiology(mock(Species.class)),
            mock(FishState.class)
        );
        Assertions.assertEquals(map.getHeight(), 2);
        Assertions.assertEquals(map.getWidth(), 2);
        //because cell range is 1 to 5, gridX should have -6 as depth (because the input goes negative only form 1 to 4)
        Assertions.assertEquals(map.getSeaTile(1, 1).getAltitude(), -1234d, .0001);
        Assertions.assertEquals(map.getSeaTile(1, 0).getAltitude(), +10, .0001);
        Assertions.assertEquals(map.getSeaTile(0, 1).getAltitude(), -6, .0001);
        Assertions.assertEquals(map.getSeaTile(0, 0).getAltitude(), -6, .0001);

    }
}
