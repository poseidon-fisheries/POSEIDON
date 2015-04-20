package uk.ac.ox.oxfish.geography;

import org.junit.Test;

import static org.junit.Assert.*;


public class CartesianDistanceTest {

    @Test
    public void simpleDistances() throws Exception {

        CartesianDistance distance = new CartesianDistance(1);

        assertEquals(distance.distance(0,0,1,1),Math.sqrt(2),.001);

        distance = new CartesianDistance(2);

        assertEquals(distance.distance(0,2,0,0),4,.001);

    }
}