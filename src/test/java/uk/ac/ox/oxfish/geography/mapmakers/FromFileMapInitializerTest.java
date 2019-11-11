package uk.ac.ox.oxfish.geography.mapmakers;

import com.vividsolutions.jts.geom.Coordinate;
import ec.util.MersenneTwisterFast;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.model.FishState;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static uk.ac.ox.oxfish.geography.mapmakers.FromFileMapInitializerFactory.DEFAULT_MAP_PADDING_IN_DEGREES;

public class FromFileMapInitializerTest {


    @Test
    public void readMapCorrectly() {
        Path path = Paths.get("inputs", "tests", "map.csv");
        FromFileMapInitializer initializer = new FromFileMapInitializer(
                path,2, DEFAULT_MAP_PADDING_IN_DEGREES, true,false
        );
        NauticalMap map = initializer.makeMap(new MersenneTwisterFast(),
                new GlobalBiology(mock(Species.class)),
                mock(FishState.class));
        assertEquals(map.getHeight(),2);
        assertEquals(map.getWidth(),2);
        //notice that
        // the coordinates flip (that's because Y is reversed in most computer coordinates)
        // the cut is somewhere at 5.5 because numbers go from 1 to 10 in the input
        assertEquals(map.getSeaTile(0,1),map.getSeaTile(new Coordinate(1,1)));
        assertEquals(map.getSeaTile(0,1),map.getSeaTile(new Coordinate(3,3)));
        assertEquals(map.getSeaTile(0,1),map.getSeaTile(new Coordinate(3,1.0001)));
        assertEquals(map.getSeaTile(0,1),map.getSeaTile(new Coordinate(1.0001,4)));
        assertEquals(map.getSeaTile(0,1),map.getSeaTile(new Coordinate(5,5)));


        assertEquals(map.getSeaTile(1,1),map.getSeaTile(new Coordinate(5.51,1)));
        assertEquals(map.getSeaTile(1,1),map.getSeaTile(new Coordinate(8,3)));
        assertEquals(map.getSeaTile(1,1),map.getSeaTile(new Coordinate(8,1)));
        assertEquals(map.getSeaTile(1,1),map.getSeaTile(new Coordinate(5.51,4)));
        assertEquals(map.getSeaTile(1,1),map.getSeaTile(new Coordinate(9,4)));
        assertEquals(map.getSeaTile(1,1),map.getSeaTile(new Coordinate(10,1)));

        //because cell range is 1 to 5, gridX should have -6 as depth (because the input goes negative only form 1 to 4)
        assertEquals(map.getSeaTile(1,1).getAltitude(),+10,.0001);
        assertEquals(map.getSeaTile(1,0).getAltitude(),+10,.0001);
        assertEquals(map.getSeaTile(0,1).getAltitude(),-6,.0001);
        assertEquals(map.getSeaTile(0,0).getAltitude(),-6,.0001);

    }
}