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

package uk.ac.ox.oxfish.model.data;

import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class MovingVarianceTest {

    @Test
    public void movingVariance() throws Exception {

        MovingVariance<Integer> variance = new MovingVariance<>(4);
        variance.addObservation(1);
        assertTrue(Double.isNaN(variance.getSmoothedObservation()));
        assertEquals(1, variance.getAverage(), .0001);
        variance.addObservation(2);
        assertEquals(1.5f, variance.getAverage(), .001f);
        assertEquals(0.25f, variance.getSmoothedObservation(), .001f);


        variance.addObservation(3);
        variance.addObservation(4);
        assertEquals(2.5f, variance.getAverage(), .001f);
        assertEquals(1.25f, variance.getSmoothedObservation(), .001f);
        variance.addObservation(5);
        assertEquals(3.5f, variance.getAverage(), .001f);
        assertEquals(1.25f, variance.getSmoothedObservation(), .001f);
        variance.addObservation(10);
        assertEquals(5.5f, variance.getAverage(), .001f);
        assertEquals(7.25f, variance.getSmoothedObservation(), .001f);
    }
}