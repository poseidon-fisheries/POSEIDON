package uk.ac.ox.oxfish.model.scenario;

import com.vividsolutions.jts.geom.Coordinate;
import org.junit.Test;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.awt.geom.Point2D;

import static org.junit.Assert.*;

/**
 * Created by carrknight on 11/17/16.
 */
public class OsmoseWFSScenarioTest {


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
//        assertEquals(latLong.getX(),25.24,.2);
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
}