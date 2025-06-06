/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2018-2025, University of Oxford.
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
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;

public class SimplexAllocator implements BiomassAllocator {

    private final double maxAllocation;

    private final double minAllocation;

    private final double bandwidth;

    private final OpenSimplexNoise noise;

    public SimplexAllocator(
        double maxAllocation, double minAllocation, double bandwidth,
        long randomSeed
    ) {
        this.maxAllocation = maxAllocation;
        this.minAllocation = minAllocation;
        this.bandwidth = bandwidth;
        this.noise = new OpenSimplexNoise(randomSeed);
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
        SeaTile tile, NauticalMap map, MersenneTwisterFast random
    ) {


        return Math.max(noise.eval(
            tile.getGridX() / bandwidth,
            tile.getGridY() / bandwidth
        ) * (maxAllocation - minAllocation) + minAllocation, 0);

    }
}
