package uk.ac.ox.oxfish.model;

import com.vividsolutions.jts.geom.Coordinate;
import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Port;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.NauticalMapFactory;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.pathfinding.StraightLinePathfinder;
import uk.ac.ox.oxfish.model.market.MarketMap;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * Testing the map itself
 * Created by carrknight on 4/3/15.
 */
public class NauticalMapTest {


    @Test
    public void readTilesDepthCorrectly() {


        NauticalMap map = NauticalMapFactory.fromBathymetryAndShapeFiles(new StraightLinePathfinder(), "test.asc", "fakempa.shp");
        //open the test grid and the test mpas

        //now, the grid ought to be simple: the altitude increases for each element from the row
        assertEquals(map.getRasterBathymetry().getGridWidth(),72);
        assertEquals(map.getRasterBathymetry().getGridHeight(),36);
        assertEquals(map.getSeaTile(0,0).getAltitude(),1,.0001);
        assertEquals(map.getSeaTile(1,0).getAltitude(),2,.0001);
        assertEquals(map.getSeaTile(71,35).getAltitude(), 2592,.0001);


        Coordinate coordinates = map.getCoordinates(71, 35);
        SeaTile tile = map.getSeaTile(coordinates);
        assertEquals(71,tile.getGridX());
        assertEquals(35,tile.getGridY());


        coordinates = map.getCoordinates(12, 13);
        tile = map.getSeaTile(coordinates);
        assertEquals(12,tile.getGridX());
        assertEquals(13,tile.getGridY());

    }

    @Test
    public void readDistancesCorrectly() {


        //test2.asc is like test.asc but it should be so that lower-left corner center grid is exactly lat0,long0 and
        //grid size is 1
        NauticalMap map = NauticalMapFactory.fromBathymetryAndShapeFiles(new StraightLinePathfinder(), "test2.asc", "fakempa.shp");
        //open the test grid and the test mpas

        //now, the grid ought to be simple: the altitude increases for each element from the row
        //here I assumed the distance is computed by the EquirectangularDistance object. Could change
        assertEquals(map.distance(0, 0, 3, 3), 471.8, .1);



    }

    @Test
    public void readMPAsCorrectly() {


        NauticalMap map = NauticalMapFactory.fromBathymetryAndShapeFiles(new StraightLinePathfinder(), "test.asc", "fakempa.shp");

        //there is only one big square MPA in the middle. So at the borders we ought not to be protected
        assertFalse(map.getSeaTile(0,0).isProtected());
        assertFalse(map.getSeaTile(1,0).isProtected());
        assertFalse(map.getSeaTile(71, 35).isProtected());
        //but right in the middle we are
        assertTrue(map.getSeaTile(35, 17).isProtected());




    }


    @Test
    public void addPortsCorrectly()
    {


        //read the 5by5 asc
        NauticalMap map = NauticalMapFactory.fromBathymetryAndShapeFiles(new StraightLinePathfinder(), "5by5.asc");
        //it has 3 sea columns and then 2 columns of land
        //I can put a port for each coastal land port
        for(int row=0; row<5; row++)
        {
            Port port = new Port("Port 0", map.getSeaTile(3, row), mock(MarketMap.class), 0);
            map.addPort(port);
            map.getPorts().contains(port);
            assertEquals(map.getPortMap().getObjectLocation(port).x,3);
            assertEquals(map.getPortMap().getObjectLocation(port).y,row);
            assertEquals(map.getPortMap().getObjectsAtLocation(3,row).size(),1);
            assertEquals(map.getPortMap().getObjectsAtLocation(3,row).get(0),port);
        }
        //no exceptions thrown
        assertEquals(5, map.getPorts().size());

    }

    @Test(expected=IllegalArgumentException.class)
    public void addPortsOnSeaIsWrong()
    {
        NauticalMap map = NauticalMapFactory.fromBathymetryAndShapeFiles(new StraightLinePathfinder(), "5by5.asc");
        map.addPort(new Port("Port 0", map.getSeaTile(2, 0), mock(MarketMap.class), 0)); //throws exception since the seatile is underwater
    }

    @Test(expected=IllegalArgumentException.class)
    public void addPortsAwayFromSeaIsWrong()
    {
        NauticalMap map = NauticalMapFactory.fromBathymetryAndShapeFiles(new StraightLinePathfinder(), "5by5.asc");
        map.addPort(new Port("Port 0", map.getSeaTile(4, 0), mock(MarketMap.class), 0)); //it's on land but there is no sea around.
    }


    @Test
    public void towCountCorrect() throws Exception {

        NauticalMap map = NauticalMapFactory.fromBathymetryAndShapeFiles(new StraightLinePathfinder(), "5by5.asc");
        FishState state = new FishState(0l);
        map.recordFishing(map.getSeaTile(2,2));
        map.recordFishing(map.getSeaTile(2,2));
        map.recordFishing(map.getSeaTile(2,3));

        assertEquals(map.getDailyTrawlsMap().get(2,2),2);
        assertEquals(map.getDailyTrawlsMap().get(2, 2), 2);
        assertEquals(map.getDailyTrawlsMap().get(2,3),1);
        assertEquals(map.getDailyTrawlsMap().get(3, 3), 0);



    }
}