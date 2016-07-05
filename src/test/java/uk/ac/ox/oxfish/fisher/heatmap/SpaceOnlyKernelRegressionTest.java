package uk.ac.ox.oxfish.fisher.heatmap;

import org.junit.Test;
import uk.ac.ox.oxfish.fisher.heatmap.regression.GeographicalObservation;
import uk.ac.ox.oxfish.fisher.heatmap.regression.SpaceOnlyKernelRegression;
import uk.ac.ox.oxfish.geography.SeaTile;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by carrknight on 6/27/16.
 */
public class SpaceOnlyKernelRegressionTest
{


    @Test
    public void regress1d() throws Exception {


        SpaceOnlyKernelRegression regression = new SpaceOnlyKernelRegression(10, 3);

        SeaTile stub = mock(SeaTile.class);
        when(stub.getGridX()).thenReturn(0);
        when(stub.getGridY()).thenReturn(0);
        regression.addObservation(new GeographicalObservation(stub, 0, 100));
        stub = mock(SeaTile.class);
        when(stub.getGridX()).thenReturn(10);
        when(stub.getGridY()).thenReturn(0);
        regression.addObservation(new GeographicalObservation(stub,0,150));
        stub = mock(SeaTile.class);
        when(stub.getGridX()).thenReturn(7);
        when(stub.getGridY()).thenReturn(0);
        regression.addObservation(new GeographicalObservation(stub,0,110));
        stub = mock(SeaTile.class);
        when(stub.getGridX()).thenReturn(15);
        when(stub.getGridY()).thenReturn(0);
        regression.addObservation(new GeographicalObservation(stub,0,200));
        stub = mock(SeaTile.class);
        when(stub.getGridX()).thenReturn(-3);
        when(stub.getGridY()).thenReturn(0);
        regression.addObservation(new GeographicalObservation(stub,0,20));
        stub = mock(SeaTile.class);
        when(stub.getGridX()).thenReturn(-6);
        when(stub.getGridY()).thenReturn(0);
        regression.addObservation(new GeographicalObservation(stub,0,0));

        //ought to be an increasing function
        for(int x=-10; x<10; x++) {
            assertTrue(regression.predict(x,0,0d) > regression.predict(x-1,0,0d));
        }

    }

    @Test
    public void regress2d() throws Exception {


        SpaceOnlyKernelRegression regression = new SpaceOnlyKernelRegression(10,3);

        SeaTile stub = mock(SeaTile.class);
        when(stub.getGridX()).thenReturn(0);
        when(stub.getGridY()).thenReturn(0);
        regression.addObservation(new GeographicalObservation(stub,0,100));
        stub = mock(SeaTile.class);
        when(stub.getGridX()).thenReturn(10);
        when(stub.getGridY()).thenReturn(10);
        regression.addObservation(new GeographicalObservation(stub,0,150));
        stub = mock(SeaTile.class);
        when(stub.getGridX()).thenReturn(7);
        when(stub.getGridY()).thenReturn(7);
        regression.addObservation(new GeographicalObservation(stub,0,110));
        stub = mock(SeaTile.class);
        when(stub.getGridX()).thenReturn(15);
        when(stub.getGridY()).thenReturn(15);
        regression.addObservation(new GeographicalObservation(stub,0,200));
        stub = mock(SeaTile.class);
        when(stub.getGridX()).thenReturn(-3);
        when(stub.getGridY()).thenReturn(-3);
        regression.addObservation(new GeographicalObservation(stub,0,20));
        stub = mock(SeaTile.class);
        when(stub.getGridX()).thenReturn(-6);
        when(stub.getGridY()).thenReturn(-6);
        regression.addObservation(new GeographicalObservation(stub,0,0));

        //ought to be an increasing function
        for(int x=-10; x<10; x++) {
            assertTrue(regression.predict(x,x,0d) > regression.predict(x-1,x-1,0d));
        }

    }

    @Test
    public void removeCorrectElement() throws Exception {


    }
}