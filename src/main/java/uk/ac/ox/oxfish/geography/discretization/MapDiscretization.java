package uk.ac.ox.oxfish.geography.discretization;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Basically a map that connects seatile ---> group and viceversa.
 * It only keeps track of seatiles with altitude < 0
 * Created by carrknight on 11/30/16.
 */
public class MapDiscretization {


    /**
     * the algorithm we use to discretize the map into groups
     */
    private final MapDiscretizer discretizer;

    /**
     * an array each containing a list of seatiles.
     * The first index of the array is a group. Each row (list) is made up of the cells
     * that are part of that group
     */
    private List<SeaTile>[] groups;

    /**
     * the "inverse" mapping that gets for each seatile which group it belongs to
     */
    private Map<SeaTile,Integer> grouped;

    /**
     *  boolean is true if at least one cell within that group
     */
    private boolean[] validGroup;


    public MapDiscretization(MapDiscretizer discretizer) {
        this.discretizer = discretizer;
    }

    public void discretize(NauticalMap map) {

        Preconditions.checkArgument(groups == null, "already asked once to discretize!");

        groups = discretizer.discretize(map);
        //filter out all land tiles
        for (int i = 0; i < groups.length; i++) {
            groups[i] = groups[i].stream().filter(tile -> tile.getAltitude() < 0).collect(Collectors.toList());
            //lock changes
            groups[i] = Collections.unmodifiableList(groups[i]);
        }
        assert groupsShareNoTile();

        //now check for each group if there is at least one seatile in them
        validGroup =  new boolean[groups.length];
        for(int i=0;  i<groups.length; i++)
            validGroup[i] = groups[i].size()>0;

        //now again go through each group
        grouped = new HashMap<>(map.getAllSeaTilesExcludingLandAsList().size());
        for (int i = 0; i < groups.length; i++)
            for (SeaTile tile : groups[i]) {
                assert !grouped.containsKey(tile);
                grouped.put(tile, i);
            }
        assert allTilesAreInAGroup(map);

    }

    private boolean groupsShareNoTile()
    {
        boolean disjoint = true;
        for(int i=0; i<groups.length; i++)
            for(int j=i+1; j<groups.length; j++)
                disjoint = disjoint && Collections.disjoint(groups[i],groups[j]);
        return  disjoint;
    }

    private boolean allTilesAreInAGroup(NauticalMap map)
    {
        boolean fine = true;
        List<SeaTile> tiles = map.getAllSeaTilesExcludingLandAsList();
        for(SeaTile tile : tiles)
            fine = fine && (getGroup(tile) != null);

        return fine;
    }


    /**
     * find out which group does this sea tile belong to
     * @param tile
     * @return
     */
    public Integer getGroup(SeaTile tile)
    {
        assert grouped.containsKey(tile);
        assert tile.getAltitude()<=0;
        return grouped.get(tile);
    }

    public int getNumberOfGroups()
    {
        return groups.length;
    }

    public boolean isValid(int groupIndex)
    {
        return validGroup[groupIndex];
    }

    public List<SeaTile> getGroup(int groupIndex)
    {
        return groups[groupIndex];
    }

}
