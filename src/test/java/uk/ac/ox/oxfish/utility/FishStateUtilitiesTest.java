package uk.ac.ox.oxfish.utility;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by carrknight on 9/11/15.
 */
public class FishStateUtilitiesTest {


    @Test
    public void logistic() throws Exception {


        Assert.assertEquals(0.9933071491,FishStateUtilities.logisticProbability(1,10,1,.5),.0001);
        Assert.assertEquals(0.119202922,FishStateUtilities.logisticProbability(1,20,.9,1),.0001);

    }
}