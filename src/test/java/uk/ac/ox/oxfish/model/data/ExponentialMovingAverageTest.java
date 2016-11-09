package uk.ac.ox.oxfish.model.data;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by carrknight on 11/9/16.
 */
public class ExponentialMovingAverageTest {


    @Test
    public void ema() throws Exception {

        ExponentialMovingAverage<Integer> averager = new ExponentialMovingAverage<>(.2);

        averager.addObservation(1);
        assertEquals(1,averager.getSmoothedObservation(),.0001);
        averager.addObservation(2);
        averager.addObservation(3);
        averager.addObservation(4);
        assertEquals(2.048,averager.getSmoothedObservation(),.0001);

    }
}