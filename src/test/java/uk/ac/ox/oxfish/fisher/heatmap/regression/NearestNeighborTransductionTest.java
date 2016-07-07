package uk.ac.ox.oxfish.fisher.heatmap.regression;

import org.junit.Test;
import uk.ac.ox.oxfish.fisher.actions.MovingTest;
import uk.ac.ox.oxfish.fisher.heatmap.regression.distance.SpaceTimeRegressionDistance;
import uk.ac.ox.oxfish.model.FishState;

import static org.junit.Assert.assertEquals;

/**
 * Created by carrknight on 7/5/16.
 */
public class NearestNeighborTransductionTest {

    @Test
    public void correctNeighbor() throws Exception
    {

        FishState state = MovingTest.generateSimple50x50Map();
        NearestNeighborTransduction regression = new NearestNeighborTransduction(1d, state.getMap(),
                                                                                 new SpaceTimeRegressionDistance(10000, 1) );
        regression.addObservation(new GeographicalObservation(state.getMap().getSeaTile(10,10), 0, 100));
        regression.addObservation(new GeographicalObservation(state.getMap().getSeaTile(0,0),0,1));
        assertEquals(regression.predict(state.getMap().getSeaTile(0,0),0,state),1,.001);
        assertEquals(regression.predict(state.getMap().getSeaTile(1,0),0,state),1,.001);
        assertEquals(regression.predict(state.getMap().getSeaTile(0,1),0,state),1,.001);
        assertEquals(regression.predict(state.getMap().getSeaTile(3,3),0,state),1,.001);
        assertEquals(regression.predict(state.getMap().getSeaTile(6,6),0,state),100,.001);
        assertEquals(regression.predict(state.getMap().getSeaTile(30,30),0,state),100,.001);







    }

}