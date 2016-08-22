package uk.ac.ox.oxfish.fisher.heatmap.regression.bayes;

import ec.util.MersenneTwisterFast;
import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.MovingTest;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.GeographicalObservation;
import uk.ac.ox.oxfish.geography.ManhattanDistance;
import uk.ac.ox.oxfish.model.FishState;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by carrknight on 8/22/16.
 */
public class GoodBadRegressionTest {


    @Test
    public void goodBadRegressionTest() throws Exception {

        FishState state = MovingTest.generateSimple50x50Map();
        //std= 2
        //avg good = 10, bad = 5

        //force initial prior to be 50-50
        MersenneTwisterFast random = mock(MersenneTwisterFast.class);
        when(random.nextDouble()).thenReturn(.5);

        GoodBadRegression regression = new GoodBadRegression(
                state.getMap(),
                new ManhattanDistance(),
                random,
                5,10,2,
                1,
                0.1
        );


        assertEquals(regression.predict(state.getMap().getSeaTile(25,25),0,mock(Fisher.class)),7.5,.001);
        //observe a 9
        regression.addObservation(
                new GeographicalObservation<>(state.getMap().getSeaTile(25,25),0,9d),
                mock(Fisher.class)
        );

        //that suggests that this is a good spot!
        //according to R the new probability ought to be .8670358
        assertEquals(regression.predict(state.getMap().getSeaTile(25,25),0,mock(Fisher.class)),
                     .8670358*10+(1-.8670358)*5,.001);

        //now what about the neighboring cell?
        //distance = 1, means rbf is 0.3678794
        //so that the std of the observation is 5.436564
        // when all is said and done the probability of being good increases only to 0.5631002
        assertEquals(regression.predict(state.getMap().getSeaTile(25,24),0,mock(Fisher.class)),
                     0.5631002*10+(1-0.5631002)*5,.001);
        assertEquals(regression.predict(state.getMap().getSeaTile(24,25),0,mock(Fisher.class)),
                     0.5631002*10+(1-0.5631002)*5,.001);

        //at distance of 2 the probability moved only to 0.5001572
        assertEquals(regression.predict(state.getMap().getSeaTile(24,24),0,mock(Fisher.class)),
                     0.5001572*10+(1-0.5001572)*5,.001);
        assertEquals(regression.predict(state.getMap().getSeaTile(23,25),0,mock(Fisher.class)),
                     0.5001572*10+(1-0.5001572)*5,.001);


        //if I step very many times the probabilities go back to being about the same
        for(int i=0; i<100; i++) {
            regression.step(state);
            System.out.println(regression.predict(state.getMap().getSeaTile(25,25),0,mock(Fisher.class)));
        }
        assertEquals(regression.predict(state.getMap().getSeaTile(25,25),0,mock(Fisher.class)),
                     .5*10+(1-.5)*5,.001);
    }
}