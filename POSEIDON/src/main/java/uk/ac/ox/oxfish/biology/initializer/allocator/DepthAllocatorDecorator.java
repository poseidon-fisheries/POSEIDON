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

package uk.ac.ox.oxfish.biology.initializer.allocator;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;

/**
 * Allocates only within a bound and within depth
 * Created by carrknight on 7/11/17.
 */
public class DepthAllocatorDecorator implements BiomassAllocator{




    private final double minDepth;

    private final double maxDepth;

    private final BiomassAllocator delegate;


    public DepthAllocatorDecorator(
            double minDepth,
            double maxDepth,
            BiomassAllocator delegate) {
        this.minDepth = minDepth;
        this.maxDepth = maxDepth;
        this.delegate = delegate;
        Preconditions.checkArgument(minDepth<=maxDepth, "allocator depth bound badly defined");
    }


    /**
     * Returns a positive number representing the weight in terms of either
     * biomass or carrying capacity (or whatever else the allocator is used for)
     *
     * @param tile   tile to allocate a weight to
     * @param map    general map information
     * @param random
     * @return
     */
    @Override
    public double allocate(
            SeaTile tile, NauticalMap map, MersenneTwisterFast random) {
        if(
                tile.getAltitude()<=0 &&
                -tile.getAltitude()>=minDepth && -tile.getAltitude()<=maxDepth
                )
            return delegate.allocate(tile, map, random);
        else
            return 0d;

    }

    /**
     * Getter for property 'minDepth'.
     *
     * @return Value for property 'minDepth'.
     */
    public double getMinDepth() {
        return minDepth;
    }

    /**
     * Getter for property 'maxDepth'.
     *
     * @return Value for property 'maxDepth'.
     */
    public double getMaxDepth() {
        return maxDepth;
    }
}
