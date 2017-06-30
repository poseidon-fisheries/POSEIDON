package uk.ac.ox.oxfish.biology.initializer.allocator;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;

/**
 * Used by some biology initializers to choose where fish ought to be
 * Created by carrknight on 6/30/17.
 */
public interface BiomassAllocator {


    /**
     * Returns a positive number representing the weight in terms of either
     * biomass or carrying capacity (or whatever else the allocator is used for)
     * @param tile tile to allocate a weight to
     * @param map general map information
     * @param random
     * @return
     */
    public double allocate(
            SeaTile tile, NauticalMap map,
            MersenneTwisterFast random);



}
