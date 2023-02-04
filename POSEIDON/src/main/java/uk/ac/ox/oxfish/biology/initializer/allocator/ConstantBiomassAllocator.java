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

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;

/**
 * easiest allocator, always returns the same weight
 * Created by carrknight on 6/30/17.
 */
public class ConstantBiomassAllocator implements BiomassAllocator {


    private final double constant;

    public ConstantBiomassAllocator() {
        constant=1;
    }

    public ConstantBiomassAllocator(double constant) {
        this.constant = constant;
    }

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
        return constant;
    }
}
