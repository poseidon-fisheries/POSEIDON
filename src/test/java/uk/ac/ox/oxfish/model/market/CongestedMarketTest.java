package uk.ac.ox.oxfish.model.market;

import org.junit.Assert;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Hold;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Anarchy;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class CongestedMarketTest
{


    @Test
    public void marketGetCongested() throws Exception {

        CongestedMarket market = new CongestedMarket(100,10,0.01,0);

        market.start(mock(FishState.class));
        //sell 10, the revenue ought to be 100
        Hold hold = mock(Hold.class);
        when(hold.getWeightOfCatchInHold(any())).thenReturn(10d);
        TradeInfo tradeInfo = market.sellFish(hold, mock(Fisher.class), new Anarchy(), mock(FishState.class),
                                                mock(Species.class));
        Assert.assertEquals(tradeInfo.getMoneyExchanged(), 100, .001);
        //sell 90, again the price ought to be steady at 10
        when(hold.getWeightOfCatchInHold(any())).thenReturn(90d);
        tradeInfo = market.sellFish(hold, mock(Fisher.class), new Anarchy(), mock(FishState.class), mock(Species.class));
        Assert.assertEquals(tradeInfo.getMoneyExchanged(),900,.001);

        //now do it once more, this time it pays less
        when(hold.getWeightOfCatchInHold(any())).thenReturn(100d);

        tradeInfo = market.sellFish(hold, mock(Fisher.class), new Anarchy(), mock(FishState.class), mock(Species.class));
        Assert.assertEquals(tradeInfo.getMoneyExchanged(),900,.001);
        Assert.assertEquals(market.getMarginalPrice(),9,.0001d);


    }


    @Test
    public void marketGetDecongested()
    {
        CongestedMarket market = new CongestedMarket(100,10,0.01,150);
        market.start(mock(FishState.class));

        //decongested
        Assert.assertEquals(market.getMarginalPrice(),10,.0001d);

        Hold hold = mock(Hold.class);
        when(hold.getWeightOfCatchInHold(any())).thenReturn(200d);
        market.sellFish(hold, mock(Fisher.class), new Anarchy(), mock(FishState.class), mock(Species.class));

        //congested
        Assert.assertEquals(market.getMarginalPrice(),9,.0001d);

        //drops by 150 each step
        market.step(mock(FishState.class));
        //decongested
        Assert.assertEquals(market.getMarginalPrice(), 10, .0001d);
        //recongested
        market.sellFish(hold, mock(Fisher.class), new Anarchy(), mock(FishState.class), mock(Species.class));
        Assert.assertEquals(market.getMarginalPrice(), 8.5, .0001d);




    }
}