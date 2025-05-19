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

package uk.ac.ox.oxfish.biology.initializer.allocator;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;

/**
 * Within the bound it allocates weight (1-x/width)^exponent
 * <p>
 * <p>
 * Created by carrknight on 6/30/17.
 */
public class FromLeftToRightBiomassAllocator implements BiomassAllocator {

    private final double lowestX;

    private final double lowestY;

    private final double highestX;

    private final double highestY;


    private final double exponent;


    public FromLeftToRightBiomassAllocator(
        double lowestX,
        double lowestY,
        double highestX,
        double highestY,
        double exponent
    ) {
        this.lowestX = lowestX;
        this.lowestY = lowestY;
        this.highestX = highestX;
        this.highestY = highestY;
        this.exponent = exponent;
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
        if (tile.getGridY() >= lowestY && tile.getGridY() <= highestY &&
            tile.getGridX() >= lowestX && tile.getGridX() <= highestX)
            return Math.pow(
                (1 - tile.getGridX() / (double) map.getWidth())
                , exponent);
        else
            return 0;
    }
}
