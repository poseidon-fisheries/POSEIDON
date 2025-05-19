/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.fisher.heatmap.regression.numerical;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.heatmap.regression.ErrorTrackingRegression;
import uk.ac.ox.oxfish.model.FishState;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by carrknight on 8/19/16.
 */
@SuppressWarnings("unchecked")
public class ErrorTrackingRegressionTest {

    @Test
    public void errorsAreStoredCorrectly() throws Exception {


        final GeographicalRegression<Double> fake = mock(GeographicalRegression.class);
        final ErrorTrackingRegression<Double> test = new ErrorTrackingRegression<Double>(fake, 3);

        when(fake.predict(any(), anyDouble(), any(), any())).thenReturn(1d, 2d, 3d, 4d);
        when(fake.extractNumericalYFromObservation(any(), any())).thenReturn(0d, 0d, 0d, 0d);

        test.addObservation(mock(GeographicalObservation.class), mock(Fisher.class), mock(FishState.class));
        test.addObservation(mock(GeographicalObservation.class), mock(Fisher.class), mock(FishState.class));
        test.addObservation(mock(GeographicalObservation.class), mock(Fisher.class), mock(FishState.class));
        test.addObservation(mock(GeographicalObservation.class), mock(Fisher.class), mock(FishState.class));

        //it should have forgotten the first error
        double sum = 0;
        for (final double errors : test.getErrors())
            sum += errors;
        Assertions.assertEquals(4 + 9 + 16, sum, .001);
        Assertions.assertEquals(test.getLatestError(), 16, .001d);

    }
}
