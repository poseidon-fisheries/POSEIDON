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
 * Assumes the map has coordinates in degrees.
 * http://www.movable-type.co.uk/scripts/latlong.html
 * Created by carrknight on 7/2/17.
 */
public class EquirectangularDistanceByCoordinate implements Distance {



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

        Coordinate startCoordinates = map.getCoordinates(start);
        double x1 = Math.toRadians(startCoordinates.x);
        double y1 = Math.toRadians(startCoordinates.y);
        Coordinate endCoordinates = map.getCoordinates(end);
        double x2 = Math.toRadians(endCoordinates.x);
        double y2 = Math.toRadians(endCoordinates.y);
        double radius = EquirectangularDistance.EARTH_RADIUS;

        double x = (x2-x1) *
                Math.cos(
                        (y1+y2)/2d
                );
        double y = y2-y1;


        return Math.sqrt(x*x+y*y) * radius;
    }
}
