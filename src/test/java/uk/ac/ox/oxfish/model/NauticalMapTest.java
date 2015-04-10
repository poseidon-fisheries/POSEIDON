package uk.ac.ox.oxfish.model;

import org.junit.Test;
import uk.ac.ox.oxfish.geography.NauticalMap;

import static org.junit.Assert.*;

/**
 * Testing the map itself
 * Created by carrknight on 4/3/15.
 */
public class NauticalMapTest {


    @Test
    public void readTilesDepthCorrectly() {


        NauticalMap map = new NauticalMap();
        //open the test grid and the test mpas
        map.initialize("test.asc", "fakempa.shp");

        //now, the grid ought to be simple: the altitude increases for each element from the row
        assertEquals(map.getRasterBathymetry().getGridWidth(),72);
        assertEquals(map.getRasterBathymetry().getGridHeight(),36);
        assertEquals(map.getSeaTile(0,0).getAltitude(),1,.0001);
        assertEquals(map.getSeaTile(1,0).getAltitude(),2,.0001);
        assertEquals(map.getSeaTile(71,35).getAltitude(), 2592,.0001);



    }

    @Test
    public void readMPAsCorrectly() {


        NauticalMap map = new NauticalMap();
        //open the test grid and the test mpas
        map.initialize("test.asc", "fakempa.shp");

        //there is only one big square MPA in the middle. So at the borders we ought not to be protected
        assertFalse(map.getSeaTile(0,0).isProtected());
        assertFalse(map.getSeaTile(1,0).isProtected());
        assertFalse(map.getSeaTile(71, 35).isProtected());
        //but right in the middle we are
        assertTrue(map.getSeaTile(35, 17).isProtected());




    }


}