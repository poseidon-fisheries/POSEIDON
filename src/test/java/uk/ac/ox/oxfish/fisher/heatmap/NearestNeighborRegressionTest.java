package uk.ac.ox.oxfish.fisher.heatmap;

import org.junit.Test;
import uk.ac.ox.oxfish.fisher.heatmap.regression.GeographicalObservation;
import uk.ac.ox.oxfish.fisher.heatmap.regression.NearestNeighborRegression;
import uk.ac.ox.oxfish.geography.SeaTile;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by carrknight on 7/1/16.
 */
public class NearestNeighborRegressionTest {


    @Test
    public void correctNeighbor() throws Exception
    {

        NearestNeighborRegression regression = new NearestNeighborRegression(1, 10000, 1);

        SeaTile tenten = mock(SeaTile.class);
        when(tenten.getGridX()).thenReturn(10);
        when(tenten.getGridY()).thenReturn(10);
        SeaTile zero = mock(SeaTile.class);
        when(zero.getGridX()).thenReturn(0);
        when(zero.getGridY()).thenReturn(0);
        regression.addObservation(new GeographicalObservation<>(tenten, 0, 100d),null );
        regression.addObservation(new GeographicalObservation<>(zero,0,1d), null);
        assertEquals(regression.predict(0,0,0),1,.001);
        assertEquals(regression.predict(1,0,0),1,.001);
        assertEquals(regression.predict(0,1,0),1,.001);
        assertEquals(regression.predict(3,3,0),1,.001);
        assertEquals(regression.predict(6,6,0),100,.001);
        assertEquals(regression.predict(30,30,0),100,.001);






    }
}