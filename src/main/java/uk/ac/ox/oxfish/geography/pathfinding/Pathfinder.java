package uk.ac.ox.oxfish.geography.pathfinding;

import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;

import java.util.Deque;

/**
 * An object tasked to return a full osmoseWFSPath from start to end
 * Created by carrknight on 11/4/15.
 */
public interface Pathfinder {


    /**
     * return the full osmoseWFSPath that brings us from start to end
     * @param map the map
     * @param start the starting tile
     * @param end the ending tile
     * @return a queue of steps from start to end or null if it isn't possible to go from start to end
     */
    Deque<SeaTile> getRoute(NauticalMap map, SeaTile start, SeaTile end);







}
