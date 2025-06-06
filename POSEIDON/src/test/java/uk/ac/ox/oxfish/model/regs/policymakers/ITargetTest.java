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

package uk.ac.ox.oxfish.model.regs.policymakers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.DataColumn;
import uk.ac.ox.oxfish.model.regs.policymakers.sensors.ITarget;

import static org.mockito.Mockito.*;

public class ITargetTest {

    @Test
    public void iTargetTest() {

        //using DLM toolkit numbers
        double[] indicators = new double[]{
            1.04158968638029, 0.859945284109567, 0.926372567057367,
            0.724816406883902, 0.708079206367097, 0.806448385440826,
            0.565583334608391, 0.635477955647245, 1.298701566065, 1.40784205791668,
            0.742410374058509, 0.727146523713908, 1.00488900317951, 1.05670327078653,
            1.1620629858966, 0.701410061340196, 0.914689667756402, 0.85874709669169,
            1.20816538836671, 0.754876423014956
        };

        double[] catches = new double[]{
            1943.63679516543, 1207.06591337924, 698.829110540912, 1179.10542991039,
            883.733005955517, 2115.37881507256, 1600.06592058985, 2166.15953303689, 1245.55026510632,
            1755.12245722454, 1566.59126894976, 837.820743292191, 1604.98156371324, 1378.27771114478,
            1737.79150132131, 1278.73589621442, 1336.22010040894, 935.4526217358, 2369.60379856847,
            1019.16842190158
        };


        DataColumn indicatorColumn = new DataColumn("indicator");
        DataColumn catchesColumn = new DataColumn("catches");
        for (double indicator : indicators) {
            indicatorColumn.add(indicator);
        }
        for (double catchestoday : catches) {
            catchesColumn.add(catchestoday);
        }

        FishState state = mock(FishState.class, RETURNS_DEEP_STUBS);
        when(state.getYearlyDataSet().getColumn("indicator")).thenReturn(indicatorColumn);
        when(state.getYearlyDataSet().getColumn("catches")).thenReturn(catchesColumn);

        ITarget target = new ITarget("catches", "indicator",
            0.2,
            1.7,
            5, 5 * 2
        );

        Assertions.assertEquals(target.scan(state), 661.2503, .0001);

    }
}
