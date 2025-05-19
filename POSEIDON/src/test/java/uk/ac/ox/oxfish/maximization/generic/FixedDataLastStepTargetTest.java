/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2018-2025, University of Oxford.
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

import java.nio.file.Paths;

import static org.mockito.Mockito.*;

public class FixedDataLastStepTargetTest {

    @Test
    public void errorComputesCorrectly() {
        final DataTarget target = FixedDataLastStepTarget.lastStepTarget(
            Paths.get("inputs", "tests", "landings.csv"), "fakeData"
        );

        //the last number there is 10702457.01
        //that should be the only number that matters!

        final FishState model = mock(FishState.class, RETURNS_DEEP_STUBS);
        final DataColumn fakeData = new DataColumn("fakeData");
        fakeData.add(100d);
        fakeData.add(100d);
        fakeData.add(100d);
        fakeData.add(100d); //only this will matter!

        when(model.getYearlyDataSet().getColumn("fakeData")).thenReturn(fakeData);

        Assertions.assertEquals(target.computeError(model), 10702357.01, .0001);

    }

    @Test
    public void onlyLastElementMatters() {
        final DataTarget target = FixedDataLastStepTarget.lastStepTarget(
            Paths.get("inputs", "tests", "landings.csv"), "fakeData"
        );

        //the last number there is 10702457.01
        //that should be the only number that matters!

        final FishState model = mock(FishState.class, RETURNS_DEEP_STUBS);
        final DataColumn fakeData = new DataColumn("fakeData");
        fakeData.add(100d);
        fakeData.add(100d);
        fakeData.add(100d);
        fakeData.add(10702357.01d); //only this will matter!

        when(model.getYearlyDataSet().getColumn("fakeData")).thenReturn(fakeData);

        Assertions.assertEquals(target.computeError(model), 100, .0001);

    }
}
