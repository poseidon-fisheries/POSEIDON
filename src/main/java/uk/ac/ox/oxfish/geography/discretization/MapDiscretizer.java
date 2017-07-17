package uk.ac.ox.oxfish.geography.discretization;

import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;

import java.util.List;
import java.util.function.Predicate;

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

    /**
     * If any filter returns false, the seatile is not meant to be grouped
     * @param filter
     */
    public void addFilter(Predicate<SeaTile> filter);

}
