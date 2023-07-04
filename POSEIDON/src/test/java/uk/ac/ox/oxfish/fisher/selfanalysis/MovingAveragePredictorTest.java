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

package uk.ac.ox.oxfish.fisher.selfanalysis;

import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.adaptation.Sensor;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@SuppressWarnings("unchecked")
public class MovingAveragePredictorTest {


    @Test
    public void notReadyGetsNaN() throws Exception {


        final Sensor<Fisher, Double> dummy = mock(Sensor.class);
        final MovingAveragePredictor predictor = MovingAveragePredictor.dailyMAPredictor("ignored", dummy, 30);
        assertEquals(Double.NaN, predictor.predict(), 0);

    }

    @Test
    public void noVarianceIsFine() throws Exception {


        final Sensor<Fisher, Double> dummy = mock(Sensor.class);


        final MovingAveragePredictor predictor = MovingAveragePredictor.perTripMAPredictor("ignored", dummy, 30);
        assertEquals(Double.NaN, predictor.predict(), 0);


        for (int i = 0; i < 30; i++) {
            when(dummy.scan(any())).thenReturn(1d);
            predictor.step(mock(FishState.class));
        }
        assertEquals(1, predictor.predict(), .0001);
        assertEquals(1, predictor.probabilityBelowThis(2), .0001);
        assertEquals(1, predictor.probabilityBelowThis(1), .0001);
        assertEquals(0, predictor.probabilityBelowThis(0.9), .0001);

    }

    @Test
    public void someVarianceIsFine() throws Exception {


        final Sensor<Fisher, Double> dummy = mock(Sensor.class);


        final MovingAveragePredictor predictor = MovingAveragePredictor.dailyMAPredictor("ignored", dummy, 10);
        assertEquals(Double.NaN, predictor.predict(), 0);


        for (int i = 1; i <= 10; i++) {
            when(dummy.scan(any())).thenReturn((double) i);
            predictor.step(mock(FishState.class));
        }

        assertEquals(5.5, predictor.predict(), .0001);
        assertEquals(0.0277555, predictor.probabilityBelowThis(0), .0001);
        assertEquals(0.430902, predictor.probabilityBelowThis(5), .0001);
        assertEquals(0.941407, predictor.probabilityBelowThis(10), .0001);

    }

    @Test
    public void predictSumsCorrectly() throws Exception {

        final Sensor<Fisher, Double> dummy = mock(Sensor.class);
        final MovingAveragePredictor predictor = MovingAveragePredictor.dailyMAPredictor("summer", dummy, 10);
        assertEquals(Double.NaN, predictor.predict(), 0);

        for (int i = 1; i <= 10; i++) {
            when(dummy.scan(any())).thenReturn((double) i);
            predictor.step(mock(FishState.class));
        }


        System.out.println(predictor.probabilitySumBelowThis(50, 10));
        //pnorm( (50-10*5.5)/(Sqrt(10)*2.872281323) ) = 0.2909945
        assertEquals(0.2909945, predictor.probabilitySumBelowThis(50, 10), .0001);
    }
}