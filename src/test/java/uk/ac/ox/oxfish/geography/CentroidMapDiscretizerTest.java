package uk.ac.ox.oxfish.geography;

import com.google.common.collect.Lists;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import org.junit.Test;
import uk.ac.ox.oxfish.fisher.actions.MovingTest;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.OsmoseWFSScenario;
import uk.ac.ox.oxfish.utility.CsvColumnsToLists;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;

import static org.junit.Assert.*;

/**
 * Created by carrknight on 11/30/16.
 */
public class CentroidMapDiscretizerTest {


    @Test
    public void centroid() throws Exception {


        FishState state = MovingTest.generateSimple4x4Map();
        state.getMap().getRasterBathymetry().setMBR(
                new Envelope(
                        0,1,
                        0,1
                )
        );

        assertEquals(new Coordinate(0.125,.875,0),state.getMap().getCoordinates(0, 0));
        assertEquals(new Coordinate(.875,0.125,0),state.getMap().getCoordinates(3, 3));


        CentroidMapDiscretizer discretizer = new CentroidMapDiscretizer(
                Lists.newArrayList(
                        new Coordinate(0,1),
                        new Coordinate(1,0)
                )
        );
        MapDiscretization discretization = new MapDiscretization(discretizer);
        discretization.discretize(state.getMap());

        assertEquals((int)discretization.getGroup(state.getMap().getSeaTile(0,0)),0);
        assertEquals((int)discretization.getGroup(state.getMap().getSeaTile(1,1)),0);
        assertEquals((int)discretization.getGroup(state.getMap().getSeaTile(0,1)),0);
        assertEquals((int)discretization.getGroup(state.getMap().getSeaTile(2,2)),1);
        assertEquals((int)discretization.getGroup(state.getMap().getSeaTile(3,3)),1);

    }


}