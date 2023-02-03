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

package uk.ac.ox.oxfish.geography.discretization;

import com.google.common.base.Preconditions;
import com.vividsolutions.jts.geom.Coordinate;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.CsvColumnsToLists;

import java.util.*;
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
            groups[i] = groups[i].stream().filter(SeaTile::isWater).collect(Collectors.toList());
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
      //  assert allTilesAreInAGroup(map); not true anymore because you could decide to ignore certain seatiles (usually wastelands)

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
        if(!grouped.containsKey(tile)) {
            return null;
        }
        assert grouped.containsKey(tile);
        assert tile.isWater();
        return grouped.get(tile);
    }

    public int getNumberOfGroups()
    {
        return groups.length;
    }

    public boolean isActive(){
        return groups != null;
    }

    public boolean isValid(int groupIndex)
    {
        return validGroup[groupIndex];
    }

    public List<SeaTile> getGroup(int groupIndex)
    {
        return groups[groupIndex];
    }

    public static MapDiscretization createDiscretization(FishState state, String centroidFile) {
        CsvColumnsToLists reader = new CsvColumnsToLists(
            centroidFile,
            ',',
            new String[]{"eastings", "northings"}
        );

        LinkedList<Double>[] lists = reader.readColumns();
        ArrayList<Coordinate> coordinates = new ArrayList<>();
        for (int i = 0; i < lists[0].size(); i++)
            coordinates.add(new Coordinate(lists[0].get(i),
                lists[1].get(i),
                0));

        CentroidMapDiscretizer discretizer = new CentroidMapDiscretizer(
            coordinates);
        MapDiscretization discretization = new MapDiscretization(
            discretizer);
        discretization.discretize(state.getMap());
        return discretization;
    }

}
