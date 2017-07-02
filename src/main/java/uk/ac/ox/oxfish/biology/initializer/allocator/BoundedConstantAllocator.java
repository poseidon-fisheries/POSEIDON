package uk.ac.ox.oxfish.biology.initializer.allocator;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;

/**
 * Returns 1 within the box (bounds included)
 * Created by carrknight on 6/30/17.
 */
public class BoundedConstantAllocator implements BiomassAllocator {


    private final double lowestX;

    private final double lowestY;

    private final double highestX;

    private final double highestY;

    private final boolean insideTheBox;

    public BoundedConstantAllocator(
            double lowestX, double lowestY,
            double highestX, double highestY, boolean insideTheBox) {
        this.insideTheBox = insideTheBox;
        Preconditions.checkArgument(lowestX<=highestX, "allocator bound badly defined");
        Preconditions.checkArgument(lowestY<=highestY,"allocator bound badly defined");
        this.lowestX = lowestX;
        this.lowestY = lowestY;
        this.highestX = highestX;
        this.highestY = highestY;
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
                tile.getGridX()>=lowestX && tile.getGridX()<=highestX)
            return insideTheBox ? 1d : 0d;
        else
            return insideTheBox ? 0d : 1d;




    }
}
