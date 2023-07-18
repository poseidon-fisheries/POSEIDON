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

import java.util.logging.Logger;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by carrknight on 3/22/16.
 */
public class CartesianUTMDistanceTest {

    @Test
    public void morroToSanFrancisco() throws Exception {
        //check that distance is more or less correct
        //Morro Bay: Easting: 695337, Northing: 3915757.7
        //San Francisco: 553454.67, 4178621.91

        final SeaTile morro = mock(SeaTile.class);
        final SeaTile sf = mock(SeaTile.class);
        final NauticalMap map = mock(NauticalMap.class);
        when(map.getCoordinates(morro)).thenReturn(new Coordinate(695337, 3915757.7));
        when(map.getCoordinates(sf)).thenReturn(new Coordinate(553454, 4178621.91));

        final CartesianUTMDistance distance = new CartesianUTMDistance();
        double km = distance.distance(morro, sf, map);
        Logger.getGlobal()
            .info(
                "the distance between Morro Bay and San Francisco is about 300km, the distance calculator thinks it is " + km);
        Assertions.assertEquals(km, 300, 10);

        //Los Angeles: Easting: 695337, Northing: 3915757.7
        //San Diego: 486713.98.7, 3616444.50

        final SeaTile la = mock(SeaTile.class);
        final SeaTile sd = mock(SeaTile.class);

        when(map.getCoordinates(la)).thenReturn(new Coordinate(387515.11, 3765635.80));
        when(map.getCoordinates(sd)).thenReturn(new Coordinate(486713.98, 3616444.50));

        km = distance.distance(la, sd, map);
        Logger.getGlobal()
            .info("the distance between Los Angeles and San Diego is about 180km, the distance calculator thinks it is " +
                km);
        Assertions.assertEquals(km, 180, 10);

    }
}