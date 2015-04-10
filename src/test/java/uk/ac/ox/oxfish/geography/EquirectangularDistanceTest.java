package uk.ac.ox.oxfish.geography;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Equirectangular!
 * Created by carrknight on 4/10/15.
 */
public class EquirectangularDistanceTest {


    @Test
    public void distanceMakesSense() throws Exception {

        // a grid starting at lat:0, long:0
        EquirectangularDistance distance = new EquirectangularDistance(0.0,1.0);

        //distance between 0,0 and 3,3 ought to be 471.8 kilometers
        Assert.assertEquals(distance.distance(0, 0, 3, 3), 471.8, .1);
        //same if I flip
        Assert.assertEquals(distance.distance(3, 3, 0, 0), 471.8, .1);
        //distance to itself is 0
        Assert.assertEquals(distance.distance(0, 0, 0, 0), 0, .1);



    }
}