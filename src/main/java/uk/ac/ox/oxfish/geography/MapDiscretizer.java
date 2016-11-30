package uk.ac.ox.oxfish.geography;

import java.util.List;

/**
 * Any object that allocates all seatiles to a group or another
 * Created by carrknight on 11/30/16.
 */
public interface MapDiscretizer {


    /**
     * assign all tiles to an array of groups (all groups must be disjoint)
     * @param map the map to discretize
     * @return an array of lists, each list representing a group.
     */
    public  List<SeaTile>[] discretize(NauticalMap map);

}
