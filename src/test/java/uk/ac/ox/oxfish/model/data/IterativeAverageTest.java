package uk.ac.ox.oxfish.model.data;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by carrknight on 11/9/16.
 */
public class IterativeAverageTest {


    @Test
    public void average() throws Exception {

        IterativeAverage<Integer> averager = new IterativeAverage<>();

        averager.addObservation(1);
        averager.addObservation(2);
        averager.addObservation(3);
        averager.addObservation(4);
        assertEquals(2.5,averager.getSmoothedObservation(),.0001);

    }
}