package uk.ac.ox.oxfish.model.market.itq;

import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.DailyFisherTimeSeries;
import uk.ac.ox.oxfish.model.regs.MonoQuotaRegulation;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class MonoQuotaPriceGeneratorTest {


    @Test
    public void testQuotaPrice() throws Exception {


        Fisher fisher = mock(Fisher.class);
        when(fisher.getDailyData()).thenReturn(mock(DailyFisherTimeSeries.class));
        FishState mock = mock(FishState.class);
        when(mock.getDayOfTheYear()).thenReturn(364);
        MonoQuotaRegulation regulation = new MonoQuotaRegulation(100, mock);
        when(fisher.getRegulation()).thenReturn(regulation);

        when(fisher.predictUnitProfit(3)).thenReturn(10d);


        MonoQuotaPriceGenerator gen = new MonoQuotaPriceGenerator(3);

        gen.start(mock, fisher);

        when(fisher.probabilityDailyCatchesBelowLevel(3,100)).thenReturn(.5);
        //.5*10
        assertEquals(5, gen.computeLambda(), .0001);

        //change quotas
        regulation.setQuotaRemaining(200);
        when(fisher.probabilityDailyCatchesBelowLevel(3,200)).thenReturn(0d);
        assertEquals(10, gen.computeLambda(), .0001);


        when(mock.getDayOfTheYear()).thenReturn(363);
        assertEquals(5, gen.computeLambda(), .0001);


    }
}