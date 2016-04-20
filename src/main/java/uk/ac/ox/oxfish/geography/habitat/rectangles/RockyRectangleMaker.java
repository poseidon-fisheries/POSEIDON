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
