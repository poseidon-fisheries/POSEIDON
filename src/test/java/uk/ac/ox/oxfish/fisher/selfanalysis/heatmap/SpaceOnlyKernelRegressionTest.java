package uk.ac.ox.oxfish.fisher.selfanalysis.heatmap;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Created by carrknight on 6/27/16.
 */
public class SpaceOnlyKernelRegressionTest
{


    @Test
    public void regress1d() throws Exception {


        SpaceOnlyKernelRegression regression = new SpaceOnlyKernelRegression(10,3);

        regression.addObservation(new GeographicalObservation(0,0,0,100));
        regression.addObservation(new GeographicalObservation(10,0,0,150));
        regression.addObservation(new GeographicalObservation(7,0,0,110));
        regression.addObservation(new GeographicalObservation(15,0,0,200));
        regression.addObservation(new GeographicalObservation(-3,0,0,20));
        regression.addObservation(new GeographicalObservation(-6,0,0,0));

        //ought to be an increasing function
        for(int x=-10; x<10; x++) {
            assertTrue(regression.predict(x,0,0d) > regression.predict(x-1,0,0d));
        }

    }

    @Test
    public void regress2d() throws Exception {


        SpaceOnlyKernelRegression regression = new SpaceOnlyKernelRegression(10,3);

        regression.addObservation(new GeographicalObservation(0,0,0,100));
        regression.addObservation(new GeographicalObservation(10,10,0,150));
        regression.addObservation(new GeographicalObservation(7,7,0,110));
        regression.addObservation(new GeographicalObservation(15,15,0,200));
        regression.addObservation(new GeographicalObservation(-3,-3,0,20));
        regression.addObservation(new GeographicalObservation(-6,-6,0,0));

        //ought to be an increasing function
        for(int x=-10; x<10; x++) {
            assertTrue(regression.predict(x,x,0d) > regression.predict(x-1,x-1,0d));
        }

    }

    @Test
    public void removeCorrectElement() throws Exception {


    }
}