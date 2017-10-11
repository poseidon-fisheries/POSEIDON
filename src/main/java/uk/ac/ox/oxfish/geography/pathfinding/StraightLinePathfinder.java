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



        assert start != null;
        assert  end != null;

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
