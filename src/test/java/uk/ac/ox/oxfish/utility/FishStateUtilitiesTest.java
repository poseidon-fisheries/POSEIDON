package uk.ac.ox.oxfish.utility;

import org.junit.Assert;
import org.junit.Test;

import java.awt.geom.Point2D;

/**
 * Created by carrknight on 9/11/15.
 */
public class FishStateUtilitiesTest {


    @Test
    public void logistic() throws Exception {


        Assert.assertEquals(0.9933071491,FishStateUtilities.logisticProbability(1,10,1,.5),.0001);
        Assert.assertEquals(0.119202922,FishStateUtilities.logisticProbability(1,20,.9,1),.0001);

    }

    @Test
    public void utmToLatLong() throws Exception {

        //somewhere on the equator
        Point2D.Double latlong = FishStateUtilities.utmToLatLong("10 N", 500000, 0);
        Assert.assertEquals(0,latlong.getX(),.01);
        Assert.assertEquals(-123,latlong.getY(),.01);
        //somewhere near bodega bay
        latlong = FishStateUtilities.utmToLatLong("10 N", 490000, 4250000);
        Assert.assertEquals(38.398165,latlong.getX(),.01);
        Assert.assertEquals(-123.11452062026362,latlong.getY(),.01);

    }
}