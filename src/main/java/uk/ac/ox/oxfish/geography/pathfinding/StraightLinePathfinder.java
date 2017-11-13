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

package uk.ac.ox.oxfish.geography.pathfinding;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;

import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;

/**
 * The default pathfinder: quick and stupid as it runs over land and everything else
 * Created by carrknight on 11/4/15.
 */
public class StraightLinePathfinder implements Pathfinder {



    private Table<SeaTile,SeaTile,LinkedList<SeaTile>> precomputedPaths = HashBasedTable.create();

    /**
     * builds a osmoseWFSPath from start to end. No weird pathfinding here, simply move diagonally then horizontally-vertically when that's not possible anymore
     * @param map the nautical map
     * @param start the start point
     * @param end the end point
     * @return a queue of tiles to pass through form start to end. Empty if starting point is the ending point
     */
    public Deque<SeaTile> getRoute(NauticalMap map, SeaTile start, SeaTile end)
    {



        assert start != null : "start " + start + ", end: " + end;
        assert  end != null: "start " + start + ", end: " + end;

        int x = start.getGridX(); int endX = end.getGridX();
        int y = start.getGridY(); int endY =end.getGridY();


        LinkedList<SeaTile> path = precomputedPaths.get(start,end);
        if(path!=null)
            return new LinkedList<>(path);

        //maybe it's available in reverse
        path = precomputedPaths.get(end,start);
        if(path != null) {
            LinkedList<SeaTile> reverse = new LinkedList<>(path);
            Collections.reverse(reverse);
            return reverse;
        }
        path = new LinkedList<>();
        path.add(start);

        while( x != endX || y!= endY)
        {

            int candidateX =  x + Integer.signum( endX - x );
            int candidateY =  y + Integer.signum( endY - y );

            //can you move your preferred way?
            SeaTile bestSeaTile = map.getSeaTile(candidateX, candidateY);
            if(bestSeaTile.getAltitude() <= 0 || bestSeaTile.isPortHere())
            {
                path.add(bestSeaTile);
                x = candidateX;
                y=candidateY;
            }
            //try to move on the x axis only then
            else if(candidateX != x && map.getSeaTile(candidateX,y).getAltitude() <= 0)
            {
                x= candidateX;
                path.add(map.getSeaTile(candidateX,y));
            }
            else if(candidateY != y && map.getSeaTile(x,candidateY).getAltitude() <= 0)
            {
                y=candidateY;
                path.add(map.getSeaTile(x,candidateY));

            }
            //otherwise just go over land!
            else
            {
                path.add(map.getSeaTile(candidateX,candidateY));
                x= candidateX;
                y=candidateY;
            }
        }


        assert path.peekLast().equals(end);
        assert path.peekFirst().equals(start);

        precomputedPaths.put(start,end,new LinkedList<>(path));

        return path;



    }
}
