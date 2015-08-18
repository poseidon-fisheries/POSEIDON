package uk.ac.ox.oxfish.model.data;

import org.junit.Test;

import static org.junit.Assert.*;


public class MovingVarianceTest {

    @Test
    public void movingVariance() throws Exception {

        MovingVariance<Integer> variance = new MovingVariance<>(4);
        for(int i=1; i<4; i++) {
            variance.addObservation(i);
            assertTrue(Double.isNaN(variance.getSmoothedObservation()));
            assertTrue(Double.isNaN(variance.getAverage()));
        }
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