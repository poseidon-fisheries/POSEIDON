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

package uk.ac.ox.oxfish.fisher.heatmap.regression.bayes;

import ec.util.MersenneTwisterFast;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by carrknight on 8/2/16.
 */
public class OneDimensionalKalmanFilterTest {
    @Test
    public void kalman() throws Exception {

        MersenneTwisterFast random = new MersenneTwisterFast();
        OneDimensionalKalmanFilter kalmanFilter = new OneDimensionalKalmanFilter(
            1, 1, 50 * 50, 0, 1
        );
        //we start at N(0,50^2)
        assertEquals(kalmanFilter.getStandardDeviation(), 50, .0001);
        assertEquals(kalmanFilter.getStateEstimate(), 0, .0001);

        //if I keep observing 30 then my state estimate should move to there and my standard deviation drop
        for (int i = 0; i < 10; i++) {
            kalmanFilter.observe(30, 5);
            System.out.println(kalmanFilter.getStateEstimate() +
                " ==== " +
                kalmanFilter.getStandardDeviation());
        }

        assertTrue(kalmanFilter.getStandardDeviation() < 1);
        assertEquals(kalmanFilter.getStateEstimate(), 30, 1);

        //if I keep elapsing time the mean stays constant but the uncertainty grows
        for (int i = 0; i < 100; i++) {
            kalmanFilter.elapseTime();
            System.out.println(kalmanFilter.getStateEstimate() +
                " ==== " +
                kalmanFilter.getStandardDeviation());
        }
        assertTrue(kalmanFilter.getStandardDeviation() > 5);
        assertEquals(kalmanFilter.getStateEstimate(), 30, 1);


    }
}