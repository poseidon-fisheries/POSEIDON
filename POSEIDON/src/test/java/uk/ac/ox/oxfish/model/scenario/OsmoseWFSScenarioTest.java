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

package uk.ac.ox.oxfish.model.scenario;

/**
 * temporarilly suspended until I can put WFS back online
 * Created by carrknight on 11/17/16.
 */
public class OsmoseWFSScenarioTest {

/*
    @Test
    public void coordinatesAreCorrect() throws Exception {


        FishState state = new FishState(System.currentTimeMillis());
        OsmoseWFSScenario scenario = new OsmoseWFSScenario();
        scenario.getBiologyInitializer().setPreInitializedConfiguration(false);
        scenario.getBiologyInitializer().setNumberOfOsmoseStepsToPulseBeforeSimulationStart(2);

        ScenarioEssentials ess = scenario.start(state);
        NauticalMap map = ess.getMap();



        //low-right is the corner [width,0]
        Coordinate coordinates = map.getCoordinates(map.getWidth()-1,map.getHeight()-1 );
        Point2D.Double latLong = FishStateUtilities.utmToLatLong("17 N", coordinates.x, coordinates.y);
        System.out.println("lat-long of low right :" + latLong);
//        assertEquals(latLong.getXCoordinate(),25.24,.2);
        assertEquals(latLong.getY(),-80.16,.2);
        coordinates = map.getCoordinates(0, 0);
        latLong = FishStateUtilities.utmToLatLong("17 N", coordinates.x, coordinates.y);
        System.out.println("lat-long of low-right :" + latLong);
        assertEquals(latLong.getX(),31,.2);
        assertEquals(latLong.getY(),-87,.2);

        //the distance in diagonal should be  927.31 km, but here we are doing the distance between
        //cell  centers rather than corners
        System.out.println(map.distance(0,0,map.getWidth()-1,map.getHeight()-1));
    }

    */
}
