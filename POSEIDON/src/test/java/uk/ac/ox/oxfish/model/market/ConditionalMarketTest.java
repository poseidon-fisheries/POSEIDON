package uk.ac.ox.oxfish.model.market;

import org.junit.Assert;
import org.junit.Test;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Hold;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.DataColumn;
import uk.ac.ox.oxfish.model.regs.Anarchy;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ConditionalMarketTest {


    @Test
    public void dataCollectedHereToo() throws Exception {


        FishState model = mock(FishState.class);
        Species test = new Species("papapapap");
        final FixedPriceMarket defaultMarket = new FixedPriceMarket(1.0);
        final FixedPriceMarket componentMarket = new FixedPriceMarket(2.0);

        Fisher one = mock(Fisher.class);
        Fisher two = mock(Fisher.class);

        ConditionalMarket market =
                new ConditionalMarket(
                        defaultMarket,
                        componentMarket,
                        new Predicate<Fisher>() {
                            @Override
                            public boolean test(Fisher fisher) {
                                return fisher.equals(two);
                            }
                        }
                );
        Species species = mock(Species.class);

        market.setSpecies(species);
        market.start(mock(FishState.class));
        //sell 10, the revenue ought to be 10 as you don't have access to non-default market
        Hold hold = mock(Hold.class);
        when(hold.getWeightOfCatchInHold(any())).thenReturn(10d);
        TradeInfo tradeInfo = market.sellFish(hold, mock(Fisher.class), new Anarchy(), mock(FishState.class),
                species);
        Assert.assertEquals(tradeInfo.getMoneyExchanged(), 10, .001);

        tradeInfo = market.sellFish(hold,one, new Anarchy(), mock(FishState.class),
                species);
        Assert.assertEquals(tradeInfo.getMoneyExchanged(), 10, .001);

        //but fisher two has access to better market

        tradeInfo = market.sellFish(hold,two, new Anarchy(), mock(FishState.class),
                species);
        Assert.assertEquals(tradeInfo.getMoneyExchanged(), 20, .001);




    }

}