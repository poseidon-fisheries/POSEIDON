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
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.FisherYearlyTimeSeries;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class FisherYearlyTimeSeriesTest {


    @Test
    public void testCash() throws Exception {

        FisherYearlyTimeSeries yearlyGatherer = new FisherYearlyTimeSeries();
        Fisher fisher = mock(Fisher.class);
        when(fisher.getHomePort()).thenReturn(mock(Port.class));
        when(fisher.getBankBalance()).thenReturn(0d);
        yearlyGatherer.start(mock(FishState.class), fisher);

        when(fisher.getBankBalance()).thenReturn(1d);
        yearlyGatherer.step(mock(FishState.class));
        when(fisher.getBankBalance()).thenReturn(2d);
        yearlyGatherer.step(mock(FishState.class));
        when(fisher.getBankBalance()).thenReturn(3d);
        yearlyGatherer.step(mock(FishState.class));
        Assertions.assertEquals(1d, yearlyGatherer.getColumn("CASH").get(0), .0001);
        Assertions.assertEquals(2d, yearlyGatherer.getColumn("CASH").get(1), .0001);
        Assertions.assertEquals(3d, yearlyGatherer.getColumn("CASH").get(2), .0001);
        Assertions.assertEquals(1d, yearlyGatherer.getColumn("NET_CASH_FLOW").get(0), .0001);
        Assertions.assertEquals(1d, yearlyGatherer.getColumn("NET_CASH_FLOW").get(1), .0001);
        Assertions.assertEquals(1d, yearlyGatherer.getColumn("NET_CASH_FLOW").get(2), .0001);


    }
}
