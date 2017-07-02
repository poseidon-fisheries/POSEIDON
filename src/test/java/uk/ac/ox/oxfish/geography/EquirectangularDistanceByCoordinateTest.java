package uk.ac.ox.oxfish.geography;

import com.vividsolutions.jts.geom.Coordinate;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
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

        NauticalMap map = mock(NauticalMap.class);
        SeaTile firstPoint = mock(SeaTile.class);
        SeaTile secondPoint = mock(SeaTile.class);
        when(map.getCoordinates(firstPoint)).thenReturn(
                new Coordinate(114.407067850409,-6.7958646820027));
        when(map.getCoordinates(secondPoint)).thenReturn(
                new Coordinate(118.274833029541,-6.7958646820027));


        EquirectangularDistanceByCoordinate distance = new EquirectangularDistanceByCoordinate();
        double distanceInKm = distance.distance(firstPoint, secondPoint, map);
        System.out.println("distance is " + distanceInKm);
        assertEquals(distanceInKm,427.1,.1);
    }

    @Test
    public void latLong2() throws Exception {

        /*
        coordinates for 0,0 are: (114.53960549340083, -6.929496652350419, 0.0)
        coordinates for 1,1 are: (114.80468077938447, -7.196760593045837, 0.0)
        the distance between 0,0 and 1,1 is: 41.7
         */
        NauticalMap map = mock(NauticalMap.class);
        SeaTile firstPoint = mock(SeaTile.class);
        SeaTile secondPoint = mock(SeaTile.class);
        when(map.getCoordinates(firstPoint)).thenReturn(
                new Coordinate(114.53960549340083,-6.929496652350419));
        when(map.getCoordinates(secondPoint)).thenReturn(
                new Coordinate(114.80468077938447,-7.196760593045837));


        EquirectangularDistanceByCoordinate distance = new EquirectangularDistanceByCoordinate();
        double distanceInKm = distance.distance(firstPoint, secondPoint, map);
        System.out.println("distance is " + distanceInKm);
        assertEquals(distanceInKm,41.7,.1);

    }
}