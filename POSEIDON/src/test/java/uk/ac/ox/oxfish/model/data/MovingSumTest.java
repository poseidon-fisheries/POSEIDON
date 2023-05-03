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

import org.junit.Assert;
import org.junit.Test;


public class MovingSumTest {


    @Test
    public void testMovingSum() throws Exception {


        MovingSum<Integer> movingSum = new MovingSum<>(3);
        Assert.assertTrue(Double.isNaN(movingSum.getSmoothedObservation()));
        movingSum.addObservation(10);
        Assert.assertEquals(10f,movingSum.getSmoothedObservation(),.0001d);
        movingSum.addObservation(20);
        Assert.assertEquals(30f,movingSum.getSmoothedObservation(),.0001d);
        movingSum.addObservation(30);
        Assert.assertEquals(60f,movingSum.getSmoothedObservation(),.0001d);
        movingSum.addObservation(40);
        Assert.assertEquals(90f,movingSum.getSmoothedObservation(),.0001d);

    }

}