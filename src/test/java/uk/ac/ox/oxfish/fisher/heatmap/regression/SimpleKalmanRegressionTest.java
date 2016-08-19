package uk.ac.ox.oxfish.fisher.heatmap.regression;

import ec.util.MersenneTwisterFast;
import org.junit.Test;
import uk.ac.ox.oxfish.fisher.actions.MovingTest;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.GeographicalObservation;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.SimpleKalmanRegression;
import uk.ac.ox.oxfish.model.FishState;

import static org.junit.Assert.assertTrue;

/**
 * Created by carrknight on 8/3/16.
 */
public class SimpleKalmanRegressionTest {


    @Test
    public void kalman() throws Exception {
        FishState state = MovingTest.generateSimple50x50Map();

        SimpleKalmanRegression regression = new SimpleKalmanRegression(
                10, 1, 0, 100, 50, .2, 0, 0, state.getMap(), new MersenneTwisterFast());

        regression.addObservation(new GeographicalObservation<>(state.getMap().getSeaTile(10, 10), 0, 100d), null );
        regression.addObservation(new GeographicalObservation<>(state.getMap().getSeaTile(5, 5), 0, 50d), null );
        regression.addObservation(new GeographicalObservation<>(state.getMap().getSeaTile(3, 3), 0, 30d), null );
        regression.addObservation(new GeographicalObservation<>(state.getMap().getSeaTile(0, 0), 0, 1d), null );
        //should smooth somewhat linearly
        assertTrue(regression.predict(state.getMap().getSeaTile(0,0), 0, null) <
                           regression.predict(state.getMap().getSeaTile(10,10), 0, null)
                                      );
        assertTrue(regression.predict(state.getMap().getSeaTile(0,0), 0, null) <
                           regression.predict(state.getMap().getSeaTile(2,2), 0, null)
        );
        assertTrue(regression.predict(state.getMap().getSeaTile(2,2), 0, null) <
                           regression.predict(state.getMap().getSeaTile(5,5), 0, null)
        );
        assertTrue(regression.predict(state.getMap().getSeaTile(5,5), 0, null) <
                           regression.predict(state.getMap().getSeaTile(10,10), 0, null)
        );


    }
}