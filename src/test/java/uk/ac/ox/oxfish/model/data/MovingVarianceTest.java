package uk.ac.ox.oxfish.model.data;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class MovingVarianceTest {

    @Test
    public void movingVariance() throws Exception {

        MovingVariance<Integer> variance = new MovingVariance<>(4);
        variance.addObservation(1);
        assertTrue(Double.isNaN(variance.getSmoothedObservation()));
        assertEquals(1,variance.getAverage(),.0001);
        variance.addObservation(2);
        assertEquals(1.5f, variance.getAverage(), .001f);
        assertEquals(0.25f, variance.getSmoothedObservation(), .001f);


        variance.addObservation(3);
        variance.addObservation(4);
        assertEquals(2.5f, variance.getAverage(), .001f);
        assertEquals(1.25f, variance.getSmoothedObservation(), .001f);
        variance.addObservation(5);
        assertEquals(3.5f, variance.getAverage(), .001f);
        assertEquals(1.25f, variance.getSmoothedObservation(), .001f);
        variance.addObservation(10);
        assertEquals(5.5f, variance.getAverage(), .001f);
        assertEquals(7.25f, variance.getSmoothedObservation(), .001f);
    }
}