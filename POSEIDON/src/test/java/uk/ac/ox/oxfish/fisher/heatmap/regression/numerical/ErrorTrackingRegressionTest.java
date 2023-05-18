/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.fisher.heatmap.regression.numerical;

import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.heatmap.regression.ErrorTrackingRegression;
import uk.ac.ox.oxfish.model.FishState;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
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

        when(fake.predict(any(), anyDouble(), any(), any())).thenReturn(1d, 2d, 3d, 4d);
        when(fake.extractNumericalYFromObservation(any(), any())).thenReturn(0d, 0d, 0d, 0d);

        test.addObservation(mock(GeographicalObservation.class), mock(Fisher.class), mock(FishState.class));
        test.addObservation(mock(GeographicalObservation.class), mock(Fisher.class), mock(FishState.class));
        test.addObservation(mock(GeographicalObservation.class), mock(Fisher.class), mock(FishState.class));
        test.addObservation(mock(GeographicalObservation.class), mock(Fisher.class), mock(FishState.class));

        //it should have forgotten the first error
        double sum = 0;
        for (double errors : test.getErrors())
            sum += errors;
        assertEquals(4 + 9 + 16, sum, .001);
        assertEquals(test.getLatestError(), 16, .001d);

    }
}