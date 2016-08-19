package uk.ac.ox.oxfish.fisher.heatmap.regression;

import org.junit.Test;
import uk.ac.ox.oxfish.fisher.actions.MovingTest;
import uk.ac.ox.oxfish.fisher.heatmap.regression.distance.SpaceTimeRegressionDistance;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.GeographicalObservation;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.NearestNeighborTransduction;
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
        NearestNeighborTransduction regression = new NearestNeighborTransduction(state.getMap(),
                                                                                 new SpaceTimeRegressionDistance(10000, 1) );
        regression.addObservation(new GeographicalObservation<>(state.getMap().getSeaTile(10, 10), 0, 100d), null );
        regression.addObservation(new GeographicalObservation<>(state.getMap().getSeaTile(0, 0), 0, 1d), null );
        assertEquals(regression.predict(state.getMap().getSeaTile(0,0), 0, null ), 1d, .001);
        assertEquals(regression.predict(state.getMap().getSeaTile(1,0), 0, null ), 1d, .001);
        assertEquals(regression.predict(state.getMap().getSeaTile(0,1), 0, null ), 1d, .001);
        assertEquals(regression.predict(state.getMap().getSeaTile(3,3), 0, null ), 1d, .001);
        assertEquals(regression.predict(state.getMap().getSeaTile(6,6), 0, null ), 100d, .001);
        assertEquals(regression.predict(state.getMap().getSeaTile(30,30), 0, null ), 100d, .001);







    }

}