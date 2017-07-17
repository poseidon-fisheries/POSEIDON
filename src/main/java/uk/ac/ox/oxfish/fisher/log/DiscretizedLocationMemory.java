package uk.ac.ox.oxfish.fisher.log;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretization;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Holds on to the last day any group of the map was last visited
 * Created by carrknight on 12/12/16.
 */
public class DiscretizedLocationMemory {




    private final MapDiscretization discretization;

    private final LinkedList<Integer>[] visits;


    public DiscretizedLocationMemory(MapDiscretization discretization) {
        this.discretization = discretization;
        this.visits = new LinkedList[discretization.getNumberOfGroups()];
        for(int i=0; i<visits.length; i++)
            visits[i] = new LinkedList<>();
    }

    public void registerVisit(int group, int day)
    {
        visits[group].add(day);
    }

    public void registerVisit(SeaTile tile, int day)
    {
        Integer group = discretization.getGroup(tile);
        if(group!=null)
        visits[group].add(day);
    }


    public int getLastDayVisited(int group) {
        return visits[group].isEmpty() ?  -10000 : visits[group].getLast();
    }

    public LinkedList<Integer> getVisits(int group){
        return visits[group];
    }

}
