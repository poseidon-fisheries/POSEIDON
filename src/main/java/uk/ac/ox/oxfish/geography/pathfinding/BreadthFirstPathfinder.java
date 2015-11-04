package uk.ac.ox.oxfish.geography.pathfinding;

import com.google.common.base.Preconditions;
import sim.util.Bag;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;

import java.util.*;

/**
 * Stepping stone to go to A*. I don't plan on using this besides testing the basics of path-finding.
 * <br>
 * Most of this comes from: http://www.redblobgames.com/pathfinding/a-star/introduction.html
 * Created by carrknight on 11/4/15.
 */
public class BreadthFirstPathfinder implements Pathfinder {
    /**
     * Keeps searching the frontier until it hits the goal. Then returns the path that leads to it.
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

        //where we will eventually put the path
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
        //build the path
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
