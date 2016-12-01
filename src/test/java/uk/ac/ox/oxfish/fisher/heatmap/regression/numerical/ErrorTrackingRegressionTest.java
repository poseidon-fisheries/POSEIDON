package uk.ac.ox.oxfish.fisher.heatmap.regression.numerical;

import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.heatmap.regression.ErrorTrackingRegression;
import uk.ac.ox.oxfish.model.FishState;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by carrknight on 8/19/16.
 */
public class ErrorTrackingRegressionTest {

    @Test
    public void errorsAreStoredCorrectly() throws Exception {


        GeographicalRegression<Double> fake = mock(GeographicalRegression.class);
        ErrorTrackingRegression<Double> test = new ErrorTrackingRegression<Double>(fake, 3);

        when(fake.predict(any(),anyDouble(),any(),any() )).thenReturn(1d, 2d, 3d, 4d);
        when(fake.extractNumericalYFromObservation(any(),any())).thenReturn(0d,0d,0d,0d);

        test.addObservation(mock(GeographicalObservation.class),mock(Fisher.class), mock(FishState.class));
        test.addObservation(mock(GeographicalObservation.class),mock(Fisher.class), mock(FishState.class));
        test.addObservation(mock(GeographicalObservation.class),mock(Fisher.class), mock(FishState.class));
        test.addObservation(mock(GeographicalObservation.class),mock(Fisher.class), mock(FishState.class));

        //it should have forgotten the first error
        double sum = 0;
        for(double errors : test.getErrors())
            sum+=errors;
        assertEquals(4+9+16,sum,.001);
        assertEquals(test.getLatestError(),16,.001d);

    }
}