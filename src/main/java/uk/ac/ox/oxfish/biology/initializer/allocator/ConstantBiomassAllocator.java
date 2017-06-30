package uk.ac.ox.oxfish.biology.initializer.allocator;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;

/**
 * easiest allocator, always returns the same weight
 * Created by carrknight on 6/30/17.
 */
public class ConstantBiomassAllocator implements BiomassAllocator {
    /**
     * Always returns 1 if the depth is below 0
     *
     * @param tile   tile to allocate a weight to
     * @param map    general map information
     * @param random
     * @return
     */
    @Override
    public double allocate(
            SeaTile tile,
            NauticalMap map,
            MersenneTwisterFast random) {
        return 1;
    }
}
