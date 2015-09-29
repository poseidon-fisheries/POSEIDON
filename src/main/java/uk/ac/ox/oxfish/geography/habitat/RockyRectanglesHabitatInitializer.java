package uk.ac.ox.oxfish.geography.habitat;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.UniformDoubleParameter;

/**
 * Creates random rectangles of purely rocky areas, and assumes everywhere else you have sandy tiles
 * Created by carrknight on 9/28/15.
 */
public class RockyRectanglesHabitatInitializer implements HabitatInitializer
{

    final private DoubleParameter rockyHeight;

    final private DoubleParameter rockyWidth;

    final private int numberOfRectangles;


    public RockyRectanglesHabitatInitializer(
            int minRockyWidth, int maxRockyWidth, int minRockyHeight, int maxRockyHeight,
            int numberOfRectangles) {
        Preconditions.checkArgument(minRockyWidth > 0);
        Preconditions.checkArgument(minRockyHeight > 0);
        Preconditions.checkArgument(maxRockyWidth >= minRockyWidth);
        Preconditions.checkArgument(maxRockyHeight >= minRockyHeight);
        this.rockyWidth = new UniformDoubleParameter(minRockyWidth,maxRockyHeight);
        this.rockyHeight = new UniformDoubleParameter(minRockyHeight,maxRockyHeight);
        this.numberOfRectangles = numberOfRectangles;
    }


    public RockyRectanglesHabitatInitializer(
            DoubleParameter rockyHeight, DoubleParameter rockyWidth,
            int numberOfRectangles) {
        this.rockyHeight = rockyHeight;
        this.rockyWidth = rockyWidth;
        this.numberOfRectangles = numberOfRectangles;
    }

    /**
     * Performs this operation on the given argument.
     *
     * @param map the input argument
     */
    @Override
    public void applyHabitats(NauticalMap map, MersenneTwisterFast random)
    {

        //here I assume everything is sandy at first. I do not force it in case at some point I want to chain a series
        //of initializers (unlikely as it is)

        final int mapHeight = map.getHeight();
        final int mapWidth = map.getWidth();

        //create numberOfAreas rectangles
        for(int i=0; i< numberOfRectangles; i++)
        {
            //create the bottom left corner
            int x = random.nextInt(mapWidth);
            int y = random.nextInt(mapHeight);

            //get rectangle size
            int rockyWidth = this.rockyWidth.apply(random).intValue();

            int rockyHeight =  this.rockyHeight.apply(random).intValue();

            //for each tile in the rectangle
            for(int w=0; w<rockyWidth; w++)
            {
                for (int h = 0; h < rockyHeight; h++)
                {

                        SeaTile tile = map.getSeaTile(x + w, y + h);
                        //if it's in the sea
                        if(tile != null && tile.getAltitude() < 0)
                            //make it rocky
                            tile.setHabitat(new TileHabitat(1d));

                }
            }

        }


    }
}
