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
