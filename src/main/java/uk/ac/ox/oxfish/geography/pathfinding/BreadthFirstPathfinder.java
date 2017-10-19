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

import com.google.common.base.Preconditions;
import sim.util.Bag;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;

import java.util.*;

/**
 * Stepping stone to go to A*. I don't plan on using this besides testing the basics of osmoseWFSPath-finding.
 * <br>
 * Most of this comes from: http://www.redblobgames.com/pathfinding/a-star/introduction.html
 * Created by carrknight on 11/4/15.
 */
public class BreadthFirstPathfinder implements Pathfinder {
    /**
     * Keeps searching the frontier until it hits the goal. Then returns the osmoseWFSPath that leads to it.
     *
     * @param map   the map
     * @param start the starting tile
     * @param end   the ending tile
     * @return a queue of steps from start to end or null if it isn't possible to go from start to end
     */
    @Override
    public Deque<SeaTile> getRoute(
            NauticalMap map, SeaTile start, SeaTile end) {

        //preconditions
        Preconditions.checkNotNull(start);
        Preconditions.checkNotNull(end);
        Preconditions.checkNotNull(map);

        //where we will eventually put the osmoseWFSPath
        LinkedList<SeaTile> path = new LinkedList<>();

        //set of tiles to observe
        Queue<SeaTile> frontier = new LinkedList<>();
        frontier.add(start);
        //edges explored
        HashMap<SeaTile,SeaTile> cameFrom= new HashMap<>();
        cameFrom.put(start,null);
        //as long as there is somewhere to explore
        while(!frontier.isEmpty())
        {
            SeaTile current = frontier.poll();
            assert current != null; //otherwise frontier would be empty

            if(current == end)
                break;

            Bag neighbors = map.getMooreNeighbors(current, 1);
            for(Object next : neighbors)
            {
                SeaTile neighbor = ((SeaTile) next);
                if(neighbor.getAltitude()<0 && !cameFrom.containsKey(neighbor)) //ignore tiles that aren't in the sea or that we explored already
                {
                    frontier.add(neighbor);
                    cameFrom.put(neighbor,current);
                }

            }

        }
        //build the osmoseWFSPath
        SeaTile current = end;
        path.add(current);
        while(current != start)
        {
            current = cameFrom.get(current);
            path.add(current);
        }
        //reverse it
        Collections.reverse(path);

        //return it!
        return path;
    }



}
