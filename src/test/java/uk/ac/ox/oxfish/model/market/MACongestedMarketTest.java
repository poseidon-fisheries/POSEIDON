package uk.ac.ox.oxfish.model.market;

import org.junit.Test;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Anarchy;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;


public class MACongestedMarketTest {


    @Test
    public void congested() throws Exception {

        MACongestedMarket oldman = new MACongestedMarket(10,1,10);
        Species species = mock(Species.class);
        oldman.setSpecies(species);

        oldman.start(mock(FishState.class));
        assertEquals(10,oldman.getMarginalPrice(),.0001d);

        TradeInfo tradeInfo = oldman.sellFishImplementation(2, mock(Fisher.class), new Anarchy(),
                                                            mock(FishState.class), species);
        assertEquals(2*10,tradeInfo.getMoneyExchanged(),.0001);
        assertEquals(2,tradeInfo.getBiomassTraded(),.0001);

        //trading on the same day there is no hit to the congestion
        tradeInfo = oldman.sellFishImplementation(2, mock(Fisher.class), new Anarchy(),
                                                  mock(FishState.class), species);
        assertEquals(2*10,tradeInfo.getMoneyExchanged(),.0001);
        assertEquals(2,tradeInfo.getBiomassTraded(),.0001);

        oldman.step(mock(FishState.class));
        //now the price ought to be 6!
        assertEquals(6,oldman.getMarginalPrice(),.0001d);

        //step it again and it should be 8
        oldman.step(mock(FishState.class));
        assertEquals(8,oldman.getMarginalPrice(),.0001d);
        tradeInfo = oldman.sellFishImplementation(100, mock(Fisher.class), new Anarchy(),
                                                  mock(FishState.class), species);
        assertEquals(100*8,tradeInfo.getMoneyExchanged(),.0001);
        assertEquals(100,tradeInfo.getBiomassTraded(),.0001);


        //now the price should be 0
        oldman.step(mock(FishState.class));
        assertEquals(0,oldman.getMarginalPrice(),.0001d);

    }
}