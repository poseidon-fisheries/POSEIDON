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

import com.vividsolutions.jts.geom.Coordinate;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Given a set of points the discretizer associates each tile with the closest point (cartesian distance).
 * Splits ties by letting the earliest centroid (in terms of insertion) win
 * Created by carrknight on 11/30/16.
 */
public class CentroidMapDiscretizer extends AbstractMapDiscretizer {


    final private ArrayList<Coordinate> centroids;


    public CentroidMapDiscretizer(final ArrayList<Coordinate> centroids) {
        this.centroids = centroids;
    }


    /**
     * return groups but only for seatiles in the tiles list (which is all the seatiles we consider valid)
     *
     * @param map   the nautical map
     * @param tiles the list of valid seatiles
     * @return groups
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public List<SeaTile>[] discretize(final NauticalMap map, final List<SeaTile> tiles) {

        final List<SeaTile>[] groups = new List[centroids.size()];
        for (int i = 0; i < groups.length; i++)
            groups[i] = new LinkedList<>();

        //for every tile look for the closest centroid
        for (final SeaTile tile : tiles) {
            //standard find the minimum index
            int minimumIndex = 0;
            final Coordinate tileCoordinates = map.getCoordinates(tile);
            double minimumDistance = distance(
                centroids.get(0).x, tileCoordinates.x,
                centroids.get(0).y, tileCoordinates.y
            );
            for (int i = 1; i < groups.length; i++) {
                final double distance = distance(
                    centroids.get(i).x, tileCoordinates.x,
                    centroids.get(i).y, tileCoordinates.y
                );
                if (distance < minimumDistance) {
                    minimumDistance = distance;
                    minimumIndex = i;
                }
            }
            //add it to that group
            groups[minimumIndex].add(tile);
        }

        return groups;
    }

    private double distance(final double x1, final double x2, final double y1, final double y2) {
        return Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2);
    }
}
