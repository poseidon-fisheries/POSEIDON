package uk.ac.ox.oxfish.fisher.strategies.departing;

import ec.util.MersenneTwisterFast;
import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.DataColumn;
import uk.ac.ox.oxfish.model.data.collectors.FisherYearlyTimeSeries;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ExitDepartingDecoratorTest {


    @Test
    public void exitDecorated() throws Exception {

        Fisher fisher = mock(Fisher.class,RETURNS_DEEP_STUBS);
        DataColumn profits = new DataColumn(FisherYearlyTimeSeries.CASH_FLOW_COLUMN);
        when(fisher.getYearlyData().getColumn(FisherYearlyTimeSeries.CASH_FLOW_COLUMN)).thenReturn(profits);


        DepartingStrategy strategy = mock(DepartingStrategy.class);
        when(strategy.shouldFisherLeavePort(any(),any(),any())).thenReturn(true);


        ExitDepartingDecorator decorator = new ExitDepartingDecorator(strategy,2);
        //at the start it delegate
        assertTrue(decorator.shouldFisherLeavePort(fisher,
                                                   mock(FishState.class),
                                                   new MersenneTwisterFast()));


        //check with fisher who had one bad year: not enough to quit!
        profits.add(-1d);
        decorator.checkIfQuit(fisher);
        assertTrue(decorator.shouldFisherLeavePort(fisher,
                                                   mock(FishState.class),
                                                   new MersenneTwisterFast()));

        //have a good year, still not quitting
        profits.add(1d);
        decorator.checkIfQuit(fisher);
        assertTrue(decorator.shouldFisherLeavePort(fisher,
                                                   mock(FishState.class),
                                                   new MersenneTwisterFast()));

        //have two bad years but non consecutively: shouldn't quit
        profits.add(-1d);
        decorator.checkIfQuit(fisher);
        assertTrue(decorator.shouldFisherLeavePort(fisher,
                                                   mock(FishState.class),
                                                   new MersenneTwisterFast()));

        //two bad years in a row: now quit!
        profits.add(-1d);
        decorator.checkIfQuit(fisher);
        assertFalse(decorator.shouldFisherLeavePort(fisher,
                                                   mock(FishState.class),
                                                   new MersenneTwisterFast()));

        //does not matter what happens next
        profits.add(1d);
        profits.add(1d);
        profits.add(1d);
        profits.add(1d);
        assertFalse(decorator.shouldFisherLeavePort(fisher,
                                                    mock(FishState.class),
                                                    new MersenneTwisterFast()));


    }
}