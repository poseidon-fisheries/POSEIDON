/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2022  CoHESyS Lab cohesys.lab@gmail.com
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

import org.junit.Test;

import static org.junit.Assert.*;

public class IterativeAgerageBackAndForthTest {

    @Test
    public void average() throws Exception {

        IterativeAgerageBackAndForth<Integer> averager = new IterativeAgerageBackAndForth<>();

        averager.addObservationfromDouble(1);
        averager.addObservationfromDouble(2);
        averager.addObservationfromDouble(3);
        averager.addObservationfromDouble(4);
        assertEquals(2.5,averager.getSmoothedObservation(),.0001);
        averager.removeObservation(4);
        assertEquals(2,averager.getSmoothedObservation(),.0001);
        averager.removeObservation(1);
        assertEquals(2.5,averager.getSmoothedObservation(),.0001);



    }

}