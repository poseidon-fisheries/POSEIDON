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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by carrknight on 7/2/17.
 */
public class EquirectangularDistanceByCoordinateTest {


    @Test
    public void latlongdistance() throws Exception {

        // (long,lat)
        //between (114.407067850409,-6.7958646820027)
        // and
        // (118.274833029541,-6.7958646820027)
        // there ought to be 427.1 km

        final NauticalMap map = mock(NauticalMap.class);
        final SeaTile firstPoint = mock(SeaTile.class);
        final SeaTile secondPoint = mock(SeaTile.class);
        when(map.getCoordinates(firstPoint)).thenReturn(
            new Coordinate(114.407067850409, -6.7958646820027));
        when(map.getCoordinates(secondPoint)).thenReturn(
            new Coordinate(118.274833029541, -6.7958646820027));


        final EquirectangularDistanceByCoordinate distance = new EquirectangularDistanceByCoordinate();
        final double distanceInKm = distance.distance(firstPoint, secondPoint, map);
        System.out.println("distance is " + distanceInKm);
        Assertions.assertEquals(distanceInKm, 427.1, .1);
    }

    @Test
    public void latLong2() throws Exception {

        /*
        coordinates for 0,0 are: (114.53960549340083, -6.929496652350419, 0.0)
        coordinates for 1,1 are: (114.80468077938447, -7.196760593045837, 0.0)
        the distance between 0,0 and 1,1 is: 41.7
         */
        final NauticalMap map = mock(NauticalMap.class);
        final SeaTile firstPoint = mock(SeaTile.class);
        final SeaTile secondPoint = mock(SeaTile.class);
        when(map.getCoordinates(firstPoint)).thenReturn(
            new Coordinate(114.53960549340083, -6.929496652350419));
        when(map.getCoordinates(secondPoint)).thenReturn(
            new Coordinate(114.80468077938447, -7.196760593045837));


        final EquirectangularDistanceByCoordinate distance = new EquirectangularDistanceByCoordinate();
        final double distanceInKm = distance.distance(firstPoint, secondPoint, map);
        System.out.println("distance is " + distanceInKm);
        Assertions.assertEquals(distanceInKm, 41.7, .1);

    }
}