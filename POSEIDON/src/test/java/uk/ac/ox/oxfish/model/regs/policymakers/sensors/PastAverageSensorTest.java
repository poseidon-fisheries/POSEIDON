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

package uk.ac.ox.oxfish.model.regs.policymakers.sensors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.DataColumn;

import static org.mockito.Mockito.*;

public class PastAverageSensorTest {


    @Test
    public void correctlyUpdates() {

        //these number come from the DLMTookkit, SimulatedData, row 1
        final double[] indicators = {0.727146523713908, 1.00488900317951, 1.05670327078653, 1.1620629858966,
            0.701410061340196, 0.914689667756402,
            //only these three matter
            0.85, 1.20, 0.75};


        DataColumn indicatorColumn = new DataColumn("indicator");
        for (double indicator : indicators) {
            indicatorColumn.add(indicator);
        }


        FishState state = mock(FishState.class, RETURNS_DEEP_STUBS);
        when(state.getYearlyDataSet().getColumn("indicator")).thenReturn(indicatorColumn);

        PastAverageSensor target =
            new PastAverageSensor(
                "indicator",
                2
            );
        Assertions.assertEquals(target.scan(state), 0.975, .0001d);

        //make sure it updates
        indicatorColumn.add(1d);
        indicatorColumn.add(2d);
        Assertions.assertEquals(target.scan(state), 1.5d, .0001d);

    }
}
