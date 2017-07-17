package uk.ac.ox.oxfish.geography.discretization;

import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;

import java.util.ArrayList;
import java.util.List;

/**
 * Splits the map in a set of rectangles, each containing a set of cells
 * Created by carrknight on 11/9/16.
 */
public class SquaresMapDiscretizer extends AbstractMapDiscretizer {

    /**
     * number of ticks on the y axis
     */
    private final int ySplits;

    /**
     * number of ticks on the x axis
     */
    private final int xSplits;


    /**
     * return groups but only for seatiles in the tiles list (which is all the seatiles we consider valid)
     *
     * @param map           the nautical map
     * @param validSeatiles the list of valid seatiles
     * @return groups
     */
    @Override
    public List<SeaTile>[] discretize(NauticalMap map, List<SeaTile> validSeatiles)
    {
        List<SeaTile>[] groups = new List[(ySplits +1)*(xSplits +1)];
        for(int i=0;  i<groups.length; i++)
            groups[i] = new ArrayList<>();


        //start splitting
        int groupWidth = (int) Math.ceil(map.getWidth() / (xSplits +1d));
        int groupHeight = (int) Math.ceil(map.getHeight() / (ySplits +1d));
        for(int x = 0; x<map.getWidth(); x++)
            for(int y = 0; y<map.getHeight(); y++)
            {
                //integer division is what we want here!
                int height = y / groupHeight;
                int width = x / groupWidth;
                int group = height * (xSplits +1) + width;
                SeaTile tile = map.getSeaTile(x, y);
                if(validSeatiles.contains(tile))
                    groups[group].add(tile);
            }

        return groups;
    }

    public SquaresMapDiscretizer(int ySplits, int xSplits) {
        this.ySplits = ySplits;
        this.xSplits = xSplits;
    }
}
