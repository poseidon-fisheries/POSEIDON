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

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;

/**
 * Class that builds a fixed number of rocky rectangles
 * Created by carrknight on 11/18/15.
 */
public class RandomRockyRectangles implements RockyRectangleMaker {


    final private DoubleParameter rectangleHeight;

    final private DoubleParameter rectangleWidth;

    final private int numberOfRectangles;


    public RandomRockyRectangles(
        final DoubleParameter rockyHeight,
        final DoubleParameter rockyWidth,
        final int numberOfRectangles
    ) {
        Preconditions.checkArgument(numberOfRectangles > 0);

        this.rectangleHeight = rockyHeight;
        this.rectangleWidth = rockyWidth;
        this.numberOfRectangles = numberOfRectangles;
    }

    /**
     * returns an array of rectangles where the habitat will be rocky
     *
     * @param random the randomizer
     * @param map    a reference to the map
     * @return the coordinates of the rectangle, the habitat initializer will fill these with rocks
     */
    @Override
    public RockyRectangle[] buildRectangles(final MersenneTwisterFast random, final NauticalMap map) {
        final RockyRectangle[] toReturn = new RockyRectangle[numberOfRectangles];
        final int mapWidth = map.getWidth();
        final int mapHeight = map.getHeight();

        for (int i = 0; i < toReturn.length; i++) {
            //create the bottom left corner
            int x;
            int y;
            do {
                x = random.nextInt(mapWidth);
                y = random.nextInt(mapHeight);
            }
            //can't be on land
            while (map.getSeaTile(x, y).isLand());


            //get rectangle size
            final int rockyWidth = (int) rectangleWidth.applyAsDouble(random);
            final int rockyHeight = (int) rectangleHeight.applyAsDouble(random);
            //return it!
            toReturn[i] = new RockyRectangle(x, y, rockyWidth, rockyHeight);
        }
        return toReturn;

    }
}
