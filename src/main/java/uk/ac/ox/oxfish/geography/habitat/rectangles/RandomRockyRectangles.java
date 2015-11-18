package uk.ac.ox.oxfish.geography.habitat.rectangles;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;

/**
 * Class that builds a fixed number of rocky rectangles
 * Created by carrknight on 11/18/15.
 */
public class RandomRockyRectangles implements   RockyRectangleMaker {


    final private DoubleParameter rectangleHeight;

    final private DoubleParameter rectangleWidth;

    final private int numberOfRectangles;


    public RandomRockyRectangles(DoubleParameter rockyHeight, DoubleParameter rockyWidth, int numberOfRectangles) {
        Preconditions.checkArgument(numberOfRectangles>0);

        this.rectangleHeight = rockyHeight;
        this.rectangleWidth = rockyWidth;
        this.numberOfRectangles = numberOfRectangles;
    }

    /**
     * returns an array of rectangles where the habitat will be rocky
     *
     * @param random the randomizer
     * @param map    a reference to the map
     * @return the coordinates of the rectangle, the habitat initializer will fill these with rocks
     */
    @Override
    public RockyRectangle[] buildRectangles(MersenneTwisterFast random, NauticalMap map)
    {
        RockyRectangle[] toReturn = new RockyRectangle[numberOfRectangles];
        int mapWidth= map.getWidth();
        int mapHeight= map.getHeight();

        for(int i=0;i<toReturn.length;i++) {
            //create the bottom left corner
            int x;
            int y;
            do {
                x = random.nextInt(mapWidth);
                y = random.nextInt(mapHeight);
            }
            //can't be on land
            while (map.getSeaTile(x, y).getAltitude() > 0);


            //get rectangle size
            int rockyWidth = rectangleWidth.apply(random).intValue();
            int rockyHeight = rectangleHeight.apply(random).intValue();
            //return it!
            toReturn[i] = new RockyRectangle(x,y,rockyWidth,rockyHeight);
        }
        return toReturn;

    }
}
