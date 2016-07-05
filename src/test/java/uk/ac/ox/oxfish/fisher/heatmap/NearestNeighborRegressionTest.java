package uk.ac.ox.oxfish.fisher.heatmap;

import org.junit.Test;
import uk.ac.ox.oxfish.fisher.heatmap.regression.GeographicalObservation;
import uk.ac.ox.oxfish.fisher.heatmap.regression.NearestNeighborRegression;

import static org.junit.Assert.assertEquals;

/**
 * Created by carrknight on 7/1/16.
 */
public class NearestNeighborRegressionTest {


    @Test
    public void correctNeighbor() throws Exception
    {

        NearestNeighborRegression regression = new NearestNeighborRegression(1, 10000, 1);

        regression.addObservation(new GeographicalObservation(10, 10, 0, 100));
        regression.addObservation(new GeographicalObservation(0,0,0,1));
        assertEquals(regression.predict(0,0,0),1,.001);
        assertEquals(regression.predict(1,0,0),1,.001);
        assertEquals(regression.predict(0,1,0),1,.001);
        assertEquals(regression.predict(3,3,0),1,.001);
        assertEquals(regression.predict(6,6,0),100,.001);
        assertEquals(regression.predict(30,30,0),100,.001);






    }
}