package uk.ac.ox.oxfish.geography.discretization;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;

import java.util.*;

/**
 *
 * 1 to 1 discretizer, that is each tile is in its own group
 * Created by carrknight on 2/6/17.
 */
public class IdentityDiscretizer implements MapDiscretizer {
    /**
     * assign all tiles to an array of groups (all groups must be disjoint)
     *
     * @param map the map to discretize
     * @return an array of lists, each list representing a group.
     */
    @Override
    public List<SeaTile>[] discretize(NauticalMap map) {

        List<SeaTile> tiles = new ArrayList<>(map.getAllSeaTilesExcludingLandAsList());
        tiles.sort(new Comparator<SeaTile>() {
            @Override
            public int compare(SeaTile o1, SeaTile o2) {
                //sort by x, and if that fails by y
                int x = Integer.compare(o1.getGridX(), o2.getGridX());
                if (x != 0)
                    return x;
                else
                    return Integer.compare(o1.getGridY(), o2.getGridY());

            }
        });

        List<SeaTile>[] groups = new List[tiles.size()];

        for(int i=0; i<tiles.size(); i++) {
            groups[i]  = ImmutableList.of(tiles.get(i));
        }


        return groups;
    }
}
