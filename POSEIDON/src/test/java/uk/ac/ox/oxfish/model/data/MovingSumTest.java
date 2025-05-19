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

package uk.ac.ox.oxfish.model.data;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


public class MovingSumTest {


    @Test
    public void testMovingSum() throws Exception {


        MovingSum<Integer> movingSum = new MovingSum<>(3);
        Assertions.assertTrue(Double.isNaN(movingSum.getSmoothedObservation()));
        movingSum.addObservation(10);
        Assertions.assertEquals(10f, movingSum.getSmoothedObservation(), .0001d);
        movingSum.addObservation(20);
        Assertions.assertEquals(30f, movingSum.getSmoothedObservation(), .0001d);
        movingSum.addObservation(30);
        Assertions.assertEquals(60f, movingSum.getSmoothedObservation(), .0001d);
        movingSum.addObservation(40);
        Assertions.assertEquals(90f, movingSum.getSmoothedObservation(), .0001d);

    }

}
