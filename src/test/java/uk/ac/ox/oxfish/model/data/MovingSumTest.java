package uk.ac.ox.oxfish.model.data;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;


public class MovingSumTest {


    @Test
    public void testMovingSum() throws Exception {


        MovingSum<Integer> movingSum = new MovingSum<>(3);
        Assert.assertTrue(Double.isNaN(movingSum.getSmoothedObservation()));
        movingSum.addObservation(10);
        Assert.assertEquals(10f,movingSum.getSmoothedObservation(),.0001d);
        movingSum.addObservation(20);
        Assert.assertEquals(30f,movingSum.getSmoothedObservation(),.0001d);
        movingSum.addObservation(30);
        Assert.assertEquals(60f,movingSum.getSmoothedObservation(),.0001d);
        movingSum.addObservation(40);
        Assert.assertEquals(90f,movingSum.getSmoothedObservation(),.0001d);

    }

}