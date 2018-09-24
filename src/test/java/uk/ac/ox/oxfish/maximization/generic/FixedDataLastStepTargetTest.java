/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2018  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.maximization.generic;

import org.junit.Test;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.DataColumn;

import java.nio.file.Paths;

import static org.junit.Assert.*;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FixedDataLastStepTargetTest {

    @Test
    public void errorComputesCorrectly() {
        DataTarget target = FixedDataLastStepTarget.lastStepTarget(
                Paths.get("inputs", "tests", "landings.csv"), "fakeData"
        );

        //the last number there is 10702457.01
        //that should be the only number that matters!

        FishState model = mock(FishState.class, RETURNS_DEEP_STUBS);
        DataColumn fakeData = new DataColumn("fakeData");
        fakeData.add(100d);
        fakeData.add(100d);
        fakeData.add(100d);
        fakeData.add(100d); //only this will matter!

        when(model.getYearlyDataSet().getColumn("fakeData")).thenReturn(fakeData);

        assertEquals(target.computeError(model),10702357.01,.0001);

    }

    @Test
    public void onlyLastElementMatters() {
        DataTarget target = FixedDataLastStepTarget.lastStepTarget(
                Paths.get("inputs", "tests", "landings.csv"), "fakeData"
        );

        //the last number there is 10702457.01
        //that should be the only number that matters!

        FishState model = mock(FishState.class, RETURNS_DEEP_STUBS);
        DataColumn fakeData = new DataColumn("fakeData");
        fakeData.add(100d);
        fakeData.add(100d);
        fakeData.add(100d);
        fakeData.add(10702357.01d); //only this will matter!

        when(model.getYearlyDataSet().getColumn("fakeData")).thenReturn(fakeData);

        assertEquals(target.computeError(model),100,.0001);

    }
}