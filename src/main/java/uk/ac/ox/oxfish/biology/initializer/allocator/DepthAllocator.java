package uk.ac.ox.oxfish.biology.initializer.allocator;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;

/**
 * Allocates only within a bound and within depth
 * Created by carrknight on 7/11/17.
 */
public class DepthAllocator implements BiomassAllocator{


    private final double lowestX;

    private final double lowestY;

    private final double highestX;

    private final double highestY;

    private final double minDepth;

    private final double maxDepth;


    public DepthAllocator(
            double lowestX,
            double lowestY,
            double highestX,
            double highestY,
            double minDepth,
            double maxDepth) {
        this.lowestX = lowestX;
        this.lowestY = lowestY;
        this.highestX = highestX;
        this.highestY = highestY;
        this.minDepth = minDepth;
        this.maxDepth = maxDepth;
        Preconditions.checkArgument(lowestX<=highestX, "allocator X bound badly defined");
        Preconditions.checkArgument(lowestY<=highestY,"allocator Y bound badly defined");
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
        if(tile.getGridY()>=lowestY && tile.getGridY()<=highestY &&
                tile.getGridX()>=lowestX && tile.getGridX()<=highestX &&
                tile.getAltitude()<=0 &&
                -tile.getAltitude()>=minDepth && -tile.getAltitude()<=maxDepth
                )
            return 1d;
        else
            return 0d;

    }

    /**
     * Getter for property 'lowestX'.
     *
     * @return Value for property 'lowestX'.
     */
    public double getLowestX() {
        return lowestX;
    }

    /**
     * Getter for property 'lowestY'.
     *
     * @return Value for property 'lowestY'.
     */
    public double getLowestY() {
        return lowestY;
    }

    /**
     * Getter for property 'highestX'.
     *
     * @return Value for property 'highestX'.
     */
    public double getHighestX() {
        return highestX;
    }

    /**
     * Getter for property 'highestY'.
     *
     * @return Value for property 'highestY'.
     */
    public double getHighestY() {
        return highestY;
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
