/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2025, University of Oxford.
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

package uk.ac.ox.oxfish.geography;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Equirectangular!
 * Created by carrknight on 4/10/15.
 */
public class EquirectangularDistanceTest {


    @Test
    public void distanceMakesSense() throws Exception {

        // a grid starting at lat:0, long:0
        final EquirectangularDistance distance = new EquirectangularDistance(0.0, 1.0);

        //distance between 0,0 and 3,3 ought to be 471.8 kilometers
        Assertions.assertEquals(distance.distance(0, 0, 3, 3), 471.8, .1);
        //same if I flip
        Assertions.assertEquals(distance.distance(3, 3, 0, 0), 471.8, .1);
        //distance to itself is 0
        Assertions.assertEquals(distance.distance(0, 0, 0, 0), 0, .1);


    }
}
