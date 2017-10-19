/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.geography.habitat.rectangles;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.geography.NauticalMap;

/**
 * Simple interface to use with RockyRectangles to build them
 * Created by carrknight on 11/18/15.
 */
public interface RockyRectangleMaker {

    /**
     * returns an array of rectangles where the habitat will be rocky
     * @param random the randomizer
     * @param map a reference to the map
     * @return the coordinates of the rectangle, the habitat initializer will fill these with rocks
     */
    RockyRectangle[] buildRectangles(
            MersenneTwisterFast random,
            NauticalMap map);


}
