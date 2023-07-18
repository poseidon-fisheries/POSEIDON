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

package uk.ac.ox.oxfish.fisher.strategies.departing;

import ec.util.MersenneTwisterFast;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.DataColumn;
import uk.ac.ox.oxfish.model.data.collectors.FisherYearlyTimeSeries;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ExitDepartingDecoratorTest {


    @Test
    public void exitDecorated() throws Exception {

        Fisher fisher = mock(Fisher.class, RETURNS_DEEP_STUBS);
        DataColumn earningsData = new DataColumn(FisherYearlyTimeSeries.EARNINGS);
        DataColumn costData = new DataColumn(FisherYearlyTimeSeries.VARIABLE_COSTS);
        when(fisher.getYearlyData().getColumn(FisherYearlyTimeSeries.EARNINGS)).thenReturn(earningsData);
        when(fisher.getYearlyData().getColumn(FisherYearlyTimeSeries.VARIABLE_COSTS)).thenReturn(costData);


        DepartingStrategy strategy = mock(DepartingStrategy.class);
        when(strategy.shouldFisherLeavePort(any(), any(), any())).thenReturn(true);


        ExitDepartingDecorator decorator = new ExitDepartingDecorator(strategy, 2);
        //at the start it delegate
        Assertions.assertTrue(decorator.shouldFisherLeavePort(
            fisher,
            mock(FishState.class),
            new MersenneTwisterFast()
        ));


        //check with fisher who had one bad year: not enough to quit!
        earningsData.add(0d);
        costData.add(1d);
        decorator.checkIfQuit(fisher);
        Assertions.assertTrue(decorator.shouldFisherLeavePort(
            fisher,
            mock(FishState.class),
            new MersenneTwisterFast()
        ));

        //have a good year, still not quitting
        earningsData.add(3d);
        costData.add(1d);
        decorator.checkIfQuit(fisher);
        Assertions.assertTrue(decorator.shouldFisherLeavePort(
            fisher,
            mock(FishState.class),
            new MersenneTwisterFast()
        ));

        //have two bad years but non consecutively: shouldn't quit
        earningsData.add(0d);
        costData.add(1d);
        decorator.checkIfQuit(fisher);
        Assertions.assertTrue(decorator.shouldFisherLeavePort(
            fisher,
            mock(FishState.class),
            new MersenneTwisterFast()
        ));

        //two bad years in a row: now quit!
        earningsData.add(0d);
        costData.add(1d);
        decorator.checkIfQuit(fisher);
        Assertions.assertFalse(decorator.shouldFisherLeavePort(
            fisher,
            mock(FishState.class),
            new MersenneTwisterFast()
        ));

        //does not matter what happens next
        earningsData.add(300d);
        costData.add(1d);
        Assertions.assertFalse(decorator.shouldFisherLeavePort(
            fisher,
            mock(FishState.class),
            new MersenneTwisterFast()
        ));


    }
}