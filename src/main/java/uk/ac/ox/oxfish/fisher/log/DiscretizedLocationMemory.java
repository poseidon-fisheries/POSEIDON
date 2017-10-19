/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

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
