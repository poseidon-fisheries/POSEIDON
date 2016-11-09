package uk.ac.ox.oxfish.geography;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;

/**
 * Splits the map in a set of rectangles, each containing a set of cells
 * Created by carrknight on 11/9/16.
 */
public class MapDiscretizer {


    /**
     * map to split
     */
    private final NauticalMap map;

    /**
     * number of ticks on the y axis
     */
    private final int verticalSplits;

    /**
     * number of ticks on the x axis
     */
    private final int horizontalSplits;

    /**
     * an array each containing a list of seatiles.
     * The first index of the array is a group. Each row (list) is made up of the cells
     * that are part of that group
     */
    private final List<SeaTile>[] groups;

    /**
     *  boolean is true if at least one cell within that group
     */
    private final boolean[] validGroup;


    public MapDiscretizer(NauticalMap map, int verticalSplits, int horizontalSplits) {
        this.map = map;
        this.verticalSplits = verticalSplits;
        this.horizontalSplits = horizontalSplits;
        Preconditions.checkArgument(verticalSplits >= 0 && horizontalSplits >=0);

        groups = new List[(verticalSplits+1)*(horizontalSplits+1)];
        for(int i=0;  i<groups.length; i++)
            groups[i] = new ArrayList<>();


        //start splitting
        int groupWidth = map.getWidth() / (horizontalSplits+1);
        int groupHeight = map.getHeight() / (verticalSplits+1);
        for(int x = 0; x<map.getWidth(); x++)
            for(int y = 0; y<map.getHeight(); y++)
            {
                //integer division is what we want here!
                int height = y / groupHeight;
                int width = x /  groupWidth;
                int group = height * (horizontalSplits+1) + width;
                groups[group].add(map.getSeaTile(x,y));
            }
        for(int i=0;  i<groups.length; i++)
            groups[i] = ImmutableList.copyOf(groups[i]);


        //now check for each group if there is at least one seatile on the sea
        validGroup =  new boolean[groups.length];
        for(int i=0;  i<groups.length; i++)
            validGroup[i] = groups[i].stream().anyMatch(tile -> tile.getAltitude()<0);

    }


    public int getNumberOfGroups()
    {
        return groups.length;
    }

    public boolean isValid(int groupIndex)
    {
        return validGroup[groupIndex];
    }

    public List<SeaTile> getGroup(int groupIndex)
    {
        return groups[groupIndex];
    }
}
