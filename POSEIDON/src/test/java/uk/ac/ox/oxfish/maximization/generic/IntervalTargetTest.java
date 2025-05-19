/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

package uk.ac.ox.oxfish.maximization.generic;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.DataColumn;

import static org.mockito.Mockito.*;

public class IntervalTargetTest {


    @Test
    public void checkLag() {

        final DataColumn tested = new DataColumn("lame");
        tested.add(1.0);
        tested.add(2.0);
        tested.add(3.0);
        tested.add(3.0);

        final IntervalTarget target = new IntervalTarget("lame",
            1.1, 2.0, 2
        );
        //should be false,false,false,true

        final FishState model = mock(FishState.class, RETURNS_DEEP_STUBS);
        when(model.getYearlyDataSet().getColumn("lame")).thenReturn(tested);
        final boolean[] test = target.test(
            model
        );

        Assertions.assertFalse(test[0]);
        Assertions.assertFalse(test[1]);
        Assertions.assertFalse(test[2]);
        Assertions.assertTrue(test[3]);


    }


    @Test
    public void checkWithoutLag() {

        final DataColumn tested = new DataColumn("lame");
        tested.add(1.0);
        tested.add(2.0);
        tested.add(3.0);
        tested.add(3.0);

        final IntervalTarget target = new IntervalTarget("lame",
            1.1, 2.0, 0
        );
        //should be false,true,false,false

        final FishState model = mock(FishState.class, RETURNS_DEEP_STUBS);
        when(model.getYearlyDataSet().getColumn("lame")).thenReturn(tested);
        final boolean[] test = target.test(
            model
        );

        Assertions.assertFalse(test[0]);
        Assertions.assertTrue(test[1]);
        Assertions.assertFalse(test[2]);
        Assertions.assertFalse(test[3]);


    }
}
