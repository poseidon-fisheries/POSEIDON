package uk.ac.ox.oxfish.fisher.selfanalysis;

import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.DailyFisherTimeSeries;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CashFlowObjectiveTest
{

    @Test
    public void correctlyFindCashFlow() throws Exception {

        //let's do it every 6 days
        CashFlowObjective objective = new CashFlowObjective(6);
        Fisher fisher = mock(Fisher.class);
        DailyFisherTimeSeries data = new DailyFisherTimeSeries(0);
        data.start(mock(FishState.class),fisher);
        when(fisher.getDailyData()).thenReturn(data);
        when(fisher.balanceXDaysAgo(anyInt())).thenCallRealMethod();

        for(int i=0; i<100; i++)
        {
            when(fisher.getBankBalance()).thenReturn(((double) i));
            data.step(mock(FishState.class));

        }
        //cash should now be 99, 6 days ago it was 93
        assertEquals(6,objective.computeCurrentFitness(fisher),.0001);
        assertEquals(6,objective.computePreviousFitness(fisher),.0001);

        //let's add some garbage
        for(int i=0; i<6; i++)
        {
            when(fisher.getBankBalance()).thenReturn(0d);
            data.step(mock(FishState.class));
        }
        //now it should be -99
        assertEquals(-99,objective.computeCurrentFitness(fisher),.0001);
        assertEquals(6,objective.computePreviousFitness(fisher),.0001);

    }
}