package uk.ac.ox.oxfish.geography.habitat;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;

/**
 * Creates random rectangles of purely rocky areas, and assumes everywhere else you have sandy tiles
 * Created by carrknight on 9/28/15.
 */
public class RockyRectanglesInitializer implements HabitatInitializer
{

    final private int maxRockyWidth;

    final private int minRockyWidth;

    final private int maxRockyHeight;

    final private int minRockyHeight;

    final private int numberOfAreas;

    final private MersenneTwisterFast random;


    public RockyRectanglesInitializer(
            int maxRockyWidth, int minRockyWidth, int maxRockyHeight, int minRockyHeight, int numberOfAreas,
            MersenneTwisterFast random) {
        Preconditions.checkArgument(minRockyWidth > 0);
        Preconditions.checkArgument(minRockyHeight > 0);
        Preconditions.checkArgument(maxRockyWidth >= minRockyWidth);
        Preconditions.checkArgument(maxRockyHeight >= minRockyHeight);
        this.maxRockyWidth = maxRockyWidth;
        this.minRockyWidth = minRockyWidth;
        this.maxRockyHeight = maxRockyHeight;
        this.minRockyHeight = minRockyHeight;
        this.numberOfAreas = numberOfAreas;
        this.random = random;
    }

    /**
     * Performs this operation on the given argument.
     *
     * @param map the input argument
     */
    @Override
    public void accept(NauticalMap map)
    {

        //here I assume everything is sandy at first. I do not force it in case at some point I want to chain a series
        //of initializers (unlikely as it is)

        final int mapHeight = map.getHeight();
        final int mapWidth = map.getWidth();

        //create numberOfAreas rectangles
        for(int i=0; i<numberOfAreas; i++)
        {
            //create the bottom left corner
            int x = random.nextInt(mapWidth);
            int y = random.nextInt(mapHeight);

            //get rectangle size
            int rockyWidth = maxRockyWidth == minRockyWidth ? minRockyWidth :
                    random.nextInt(maxRockyWidth-minRockyWidth) + minRockyWidth;

            int rockyHeight = maxRockyHeight == minRockyHeight ? minRockyHeight :
                    random.nextInt(maxRockyHeight-minRockyHeight) + minRockyHeight;

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
