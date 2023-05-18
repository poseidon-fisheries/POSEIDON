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

package uk.ac.ox.oxfish.geography;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * Assuming the UTM map is small enough that cartesian distance is applicable to it
 * Created by carrknight on 3/18/16.
 */
public class CartesianUTMDistance implements Distance {


    /**
     * the distance between two sea-tiles
     *
     * @param start starting sea-tile
     * @param end   ending sea-tile
     * @param map
     * @return kilometers between the two
     */
    @Override
    public double distance(SeaTile start, SeaTile end, NauticalMap map) {
        Coordinate coordinate = map.getCoordinates(start);
        double xStart = coordinate.x;
        double yStart = coordinate.y;
        coordinate = map.getCoordinates(end);
        double xEnd = coordinate.x;
        double yEnd = coordinate.y;
        //this should return the distance in kilometers!
        return Math.sqrt(Math.pow(yEnd - yStart, 2) + Math.pow(xEnd - xStart, 2)) / 1000;

    }
}
