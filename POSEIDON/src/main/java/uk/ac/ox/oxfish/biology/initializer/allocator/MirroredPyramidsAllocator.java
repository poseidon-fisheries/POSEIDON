/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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
 * Basically put the fish where the pyramid isn't. Useful to put some form of anti-correlation when multiple species are around
 */
public class MirroredPyramidsAllocator implements BiomassAllocator {

    private final PyramidsAllocator originalPyramid;

    private final double noiseLevel;


    public MirroredPyramidsAllocator(PyramidsAllocator originalPyramid, double noiseLevel) {
        this.originalPyramid = originalPyramid;
        this.noiseLevel = noiseLevel;
    }


    @Override
    public double allocate(SeaTile tile, NauticalMap map, MersenneTwisterFast random) {
        if (tile.isLand())
            return 0d;


        return 1d - originalPyramid.allocate(tile, map, random) / originalPyramid.getPeakBiomass() +
            random.nextDouble() * noiseLevel;

    }

    public double getNoiseLevel() {
        return noiseLevel;
    }
}
