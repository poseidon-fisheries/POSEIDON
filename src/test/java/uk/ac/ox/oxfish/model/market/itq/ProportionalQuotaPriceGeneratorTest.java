package uk.ac.ox.oxfish.model.market.itq;

import org.junit.Assert;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.strategies.discarding.NoDiscarding;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.FisherDailyTimeSeries;

import java.util.Arrays;
import java.util.HashMap;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class ProportionalQuotaPriceGeneratorTest {


    @Test
    public void oneToOneQuota() throws Exception {


        HashMap<Integer,ITQOrderBook> orderBooks = new HashMap<>();
        orderBooks.put(0,mock(ITQOrderBook.class));
        orderBooks.put(1,mock(ITQOrderBook.class));

        FishState state = mock(FishState.class);
        when(state.getDayOfTheYear()).thenReturn(364); //one day left!
        when(state.getSpecies()).thenReturn(Arrays.asList(new Species("a"), new Species("b")));



        Fisher fisher = mock(Fisher.class);
        when(fisher.getDailyData()).thenReturn(mock(FisherDailyTimeSeries.class));
        when(fisher.predictDailyCatches(0)).thenReturn(100d);
        when(fisher.predictDailyCatches(1)).thenReturn(100d);
        when(fisher.getDiscardingStrategy()).thenReturn(new NoDiscarding());

        when(fisher.probabilitySumDailyCatchesBelow(0,123,1)).thenReturn(.5); //50% chance of needing it

        when(fisher.predictUnitProfit(0)).thenReturn(1d);
        when(fisher.predictUnitProfit(1)).thenReturn(2d);
        when(orderBooks.get(1).getLastClosingPrice()).thenReturn(.5d);

        // .5 * ( 1 + (2 -.5)) = 2
        ProportionalQuotaPriceGenerator quota = new ProportionalQuotaPriceGenerator(orderBooks,
                                                                                    0,
                                                                                    fisher1 -> 123d);
        quota.start(state,fisher);
        Assert.assertEquals(1.25d,quota.computeLambda(),.001d);


        //now set the proportion to 1:3 (specie 0 is a choke specie)
        when(fisher.predictDailyCatches(1)).thenReturn(300d);
        //this increases the value of this quota, all things constant
        // .5 * ( 1 + 3*(2 -.5)) = 2
        Assert.assertEquals(2.75d,quota.computeLambda(),.001d);





    }
}