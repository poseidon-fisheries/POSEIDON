package uk.ac.ox.oxfish.fisher.log;

import uk.ac.ox.oxfish.geography.MapDiscretization;
import uk.ac.ox.oxfish.geography.SeaTile;

import java.util.Arrays;

/**
 * Holds on to the last day any group of the map was last visited
 * Created by carrknight on 12/12/16.
 */
public class DiscretizedLocationMemory {




    private final MapDiscretization discretization;

    private final int[] lastDayVisited;


    public DiscretizedLocationMemory(MapDiscretization discretization) {
        this.discretization = discretization;
        this.lastDayVisited = new int[discretization.getNumberOfGroups()];
        Arrays.fill(lastDayVisited,-10000);
    }

    public void registerVisit(int group, int day)
    {
        lastDayVisited[group] = day;
    }

    public void registerVisit(SeaTile tile, int day)
    {
        lastDayVisited[discretization.getGroup(tile)]=day;
    }


    public int[] getLastDayVisited() {
        return lastDayVisited;
    }

}
