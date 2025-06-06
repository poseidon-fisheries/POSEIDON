/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.model;

import com.vividsolutions.jts.geom.Coordinate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.NauticalMapFactory;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.pathfinding.StraightLinePathfinder;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.market.MarketMap;

import static org.mockito.Mockito.mock;

/**
 * Testing the map itself
 * Created by carrknight on 4/3/15.
 */
public class NauticalMapTest {


    @Test
    public void readTilesDepthCorrectly() {


        final NauticalMap map = NauticalMapFactory.fromBathymetryAndShapeFiles(
            new StraightLinePathfinder(),
            "test.asc",
            "fakempa.shp"
        );
        //open the test grid and the test mpas

        //now, the grid ought to be simple: the altitude increases for each element from the row
        Assertions.assertEquals(map.getRasterBathymetry().getGridWidth(), 72);
        Assertions.assertEquals(map.getRasterBathymetry().getGridHeight(), 36);
        Assertions.assertEquals(map.getSeaTile(0, 0).getAltitude(), 1, .0001);
        Assertions.assertEquals(map.getSeaTile(1, 0).getAltitude(), 2, .0001);
        Assertions.assertEquals(map.getSeaTile(71, 35).getAltitude(), 2592, .0001);


        Coordinate coordinates = map.getCoordinates(71, 35);
        SeaTile tile = map.getSeaTile(coordinates);
        Assertions.assertEquals(71, tile.getGridX());
        Assertions.assertEquals(35, tile.getGridY());


        coordinates = map.getCoordinates(12, 13);
        tile = map.getSeaTile(coordinates);
        Assertions.assertEquals(12, tile.getGridX());
        Assertions.assertEquals(13, tile.getGridY());

    }

    @Test
    public void readDistancesCorrectly() {


        //test2.asc is like test.asc but it should be so that lower-left corner center grid is exactly lat0,long0 and
        //grid size is 1
        final NauticalMap map = NauticalMapFactory.fromBathymetryAndShapeFiles(
            new StraightLinePathfinder(),
            "test2.asc",
            "fakempa.shp"
        );
        //open the test grid and the test mpas

        //now, the grid ought to be simple: the altitude increases for each element from the row
        //here I assumed the distance is computed by the EquirectangularDistance object. Could change
        Assertions.assertEquals(map.distance(0, 0, 3, 3), 471.8, .1);


    }

    @Test
    public void readMPAsCorrectly() {


        final NauticalMap map = NauticalMapFactory.fromBathymetryAndShapeFiles(
            new StraightLinePathfinder(),
            "test.asc",
            "fakempa.shp"
        );

        //there is only one big square MPA in the middle. So at the borders we ought not to be protected
        Assertions.assertFalse(map.getSeaTile(0, 0).isProtected());
        Assertions.assertFalse(map.getSeaTile(1, 0).isProtected());
        Assertions.assertFalse(map.getSeaTile(71, 35).isProtected());
        //but right in the middle we are
        Assertions.assertTrue(map.getSeaTile(35, 17).isProtected());


    }


    @Test
    public void addPortsCorrectly() {


        //read the 5by5 asc
        final NauticalMap map = NauticalMapFactory.fromBathymetryAndShapeFiles(
            new StraightLinePathfinder(),
            "5by5.asc"
        );
        //it has 3 sea columns and then 2 columns of land
        //I can put a port for each coastal land port
        for (int row = 0; row < 5; row++) {
            final Port port = new Port("Port 0", map.getSeaTile(3, row), mock(MarketMap.class), 0);
            map.addPort(port);
            map.getPorts().contains(port);
            Assertions.assertEquals(map.getPortMap().getObjectLocation(port).x, 3);
            Assertions.assertEquals(map.getPortMap().getObjectLocation(port).y, row);
            Assertions.assertEquals(map.getPortMap().getObjectsAtLocation(3, row).size(), 1);
            Assertions.assertEquals(map.getPortMap().getObjectsAtLocation(3, row).get(0), port);
        }
        //no exceptions thrown
        Assertions.assertEquals(5, map.getPorts().size());

    }

    public void addPortsOnSeaIsWrong() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            final NauticalMap map = NauticalMapFactory.fromBathymetryAndShapeFiles(
                new StraightLinePathfinder(),
                "5by5.asc"
            );
            map.addPort(new Port(
                "Port 0",
                map.getSeaTile(2, 0),
                mock(MarketMap.class),
                0
            )); //throws exception since the seatile is underwater
        });
    }

    public void addPortsAwayFromSeaIsWrong() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            final NauticalMap map = NauticalMapFactory.fromBathymetryAndShapeFiles(
                new StraightLinePathfinder(),
                "5by5.asc"
            );
            map.addPort(new Port(
                "Port 0",
                map.getSeaTile(4, 0),
                mock(MarketMap.class),
                0
            )); //it's on land but there is no sea around.
        });
    }


    @Test
    public void towCountCorrect() throws Exception {

        final NauticalMap map = NauticalMapFactory.fromBathymetryAndShapeFiles(
            new StraightLinePathfinder(),
            "5by5.asc"
        );
        final FishState state = new FishState(0L);
        map.recordFishing(map.getSeaTile(2, 2));
        map.recordFishing(map.getSeaTile(2, 2));
        map.recordFishing(map.getSeaTile(2, 3));

        Assertions.assertEquals(map.getDailyTrawlsMap().get(2, 2), 2);
        Assertions.assertEquals(map.getDailyTrawlsMap().get(2, 2), 2);
        Assertions.assertEquals(map.getDailyTrawlsMap().get(2, 3), 1);
        Assertions.assertEquals(map.getDailyTrawlsMap().get(3, 3), 0);


    }
}
