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
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import sim.util.Bag;
import uk.ac.ox.oxfish.geography.Distance;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;

import java.util.*;

/**
 * The A* pathfinder, as implemented here:
 * http://www.redblobgames.com/pathfinding/a-star/introduction.html
 * Created by carrknight on 11/4/15.
 */
public class AStarPathfinder implements Pathfinder {


    private final Distance distanceFunction;


    private final Table<SeaTile,SeaTile,Optional<LinkedList<SeaTile>>> memory = HashBasedTable.create();

    /**
     * creates the A* pathfinder it uses distanceFunction both for computing the cost of moving from A to its neighbors
     * and as a straight osmoseWFSPath heuristic
     * @param distanceFunction
     */
    public AStarPathfinder(Distance distanceFunction) {
        this.distanceFunction = distanceFunction;
    }

    /**
     * return the full osmoseWFSPath that brings us from start to end
     *
     * @param map   the map
     * @param start the starting tile
     * @param end   the ending tile
     * @return a queue of steps from start to end or null if it isn't possible to go from start to end
     */
    @Override
    public Deque<SeaTile> getRoute(
            NauticalMap map, SeaTile start, SeaTile end)
    {



        //preconditions
        Preconditions.checkNotNull(start);
        Preconditions.checkNotNull(end);
        Preconditions.checkNotNull(map);


        Optional<LinkedList<SeaTile>> oldPath = memory.get(start, end);
        if(oldPath!=null) {
            return oldPath.map(LinkedList::new).orElse(null);
        }

        //where we will eventually put the osmoseWFSPath
        LinkedList<SeaTile> path = new LinkedList<>();

        //set of tiles to observe
        PriorityQueue<FrontierElement> frontier = new PriorityQueue<>();
        frontier.add(new FrontierElement(start,0d));
        //edges explored
        HashMap<SeaTile,SeaTile> cameFrom= new HashMap<>();
        //best osmoseWFSPath cost to here so far
        HashMap<SeaTile,Double> costSoFar = new HashMap<>();
        costSoFar.put(start,0d);


        //go!
        while(!frontier.isEmpty())
        {
            //get the next element
            SeaTile current = frontier.poll().getTile();
            assert  current!=null;
            //stop if we have arrived
            if(current == end)
                break;

            //get all your neighbors
            Bag neighbors = map.getMooreNeighbors(current, 1);
            for(Object next : neighbors)
            {
                SeaTile neighbor = ((SeaTile) next);

                if(neighbor.getAltitude() >= 0 && neighbor != end) //don't bother if it's land
                    continue;

                //check how much it would cost to move there
                double newCost = costSoFar.get(current) + distanceFunction.distance(current,neighbor,map );
                if(!cameFrom.containsKey(neighbor) || newCost < costSoFar.get(neighbor)) //ignore tiles that aren't in the sea or that we explored already
                {
                    costSoFar.put(neighbor,newCost);
                    double priority = newCost + distanceFunction.distance(end,neighbor,map );
                    frontier.add(new FrontierElement(neighbor,priority));
                    cameFrom.put(neighbor,current);
                }

            }


        }

        //if you haven't found the osmoseWFSPath, then return null
        if(cameFrom.get(end) == null)
        {
            memory.put(start,end,Optional.empty());
            memory.put(end,start,Optional.empty());
            return null;
        }
        //build the osmoseWFSPath
        SeaTile current = end;
        path.add(current);
        while(current != start)
        {
            current = cameFrom.get(current);
            assert current!=null;
            path.add(current);
        }
        //reverse it
        memory.put(end,start,Optional.of(new LinkedList<>(path)));
        Collections.reverse(path);
        memory.put(start,end,Optional.of(new LinkedList<>(path)));

        //return it!
        return path;
    }


    private class FrontierElement implements Comparable<FrontierElement>
    {


        private final SeaTile tile;

        private final Double priority;


        public FrontierElement(SeaTile tile, Double priority) {
            this.tile = tile;
            this.priority = priority;
        }

        public SeaTile getTile() {
            return tile;
        }

        public Double getPriority() {
            return priority;
        }

        @Override
        public int compareTo(FrontierElement o) {
            return Double.compare(this.priority,o.priority);
        }
    }
}
