package uk.ac.ox.oxfish.geography;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Splits the map in a set of rectangles, each containing a set of cells
 * Created by carrknight on 11/9/16.
 */
public class SquaresMapDiscretizer implements MapDiscretizer {

    /**
     * number of ticks on the y axis
     */
    private final int verticalSplits;

    /**
     * number of ticks on the x axis
     */
    private final int horizontalSplits;


    @Override
    public List<SeaTile>[] discretize(NauticalMap map) {
        List<SeaTile>[] groups = new List[(verticalSplits+1)*(horizontalSplits+1)];
        for(int i=0;  i<groups.length; i++)
            groups[i] = new ArrayList<>();


        //start splitting
        int groupWidth = (int) Math.ceil(map.getWidth() / (horizontalSplits+1d));
        int groupHeight = (int) Math.ceil(map.getHeight() / (verticalSplits+1d));
        for(int x = 0; x<map.getWidth(); x++)
            for(int y = 0; y<map.getHeight(); y++)
            {
                //integer division is what we want here!
                int height = y / groupHeight;
                int width = x / groupWidth;
                int group = height * (horizontalSplits+1) + width;
                groups[group].add(map.getSeaTile(x,y));
            }

        return groups;
    }

    public SquaresMapDiscretizer(int verticalSplits, int horizontalSplits) {
        this.verticalSplits = verticalSplits;
        this.horizontalSplits = horizontalSplits;
    }
}
