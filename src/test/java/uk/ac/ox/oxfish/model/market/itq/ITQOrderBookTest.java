package uk.ac.ox.oxfish.model.market.itq;

import ec.util.MersenneTwisterFast;
import org.jfree.util.Log;
import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.MonoQuotaRegulation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;


public class ITQOrderBookTest {


    @Test
    public void crossingQuote() throws Exception
    {


        ITQOrderBook orderBook = new ITQOrderBook(0, 0, (ask, bids,ignored1,ignore2d) -> ask, 7);
        FishState state = mock(FishState.class); when(state.getYear()).thenReturn(0);
        when(state.getRandom()).thenReturn(new MersenneTwisterFast());
        orderBook.start(state);
        orderBook.setMarkup(.1);
        orderBook.setUnitsTradedPerMatch(100);

        //first guy values quota 100: he will buy for 90 and sell for 110
        MonoQuotaRegulation buyerReg = new MonoQuotaRegulation(100);
        Fisher buyer = mock(Fisher.class);
        when(buyer.getRegulation()).thenReturn(buyerReg);
        MonoQuotaPriceGenerator buyerGenerator = mock(MonoQuotaPriceGenerator.class);
        when(buyerGenerator.computeLambda()).thenReturn(100d);
        orderBook.registerTrader(buyer,buyerGenerator);

        //the second guy values quota 10: he will buy for 9 and sell for 11
        MonoQuotaRegulation sellerReg = new MonoQuotaRegulation(100);
        Fisher seller = mock(Fisher.class);
        when(seller.getRegulation()).thenReturn(sellerReg);
        MonoQuotaPriceGenerator sellerGenerator = mock(MonoQuotaPriceGenerator.class);
        when(sellerGenerator.computeLambda()).thenReturn(10d);
        orderBook.registerTrader(seller, sellerGenerator);

        orderBook.step(state);
        //they should have exchanged quotas!
        assertEquals(buyerReg.getQuotaRemaining(0), 200,.0001);
        assertEquals(sellerReg.getQuotaRemaining(0),0,.0001);
        assertEquals(orderBook.getDailyMatches(),1,.0001);
        assertEquals(orderBook.getDailyAveragePrice(), 11, .0001);
        assertEquals(orderBook.getDailyQuotasExchanged(),100,.0001);
        //they should have traded  (assuming they use seller quote)
        verify(buyer).spendExogenously( 11 * 100);
        verify(seller).earn( 11 * 100);

        assertTrue(orderBook.inPenaltyBox(buyer));
        assertTrue(!orderBook.inPenaltyBox(seller));

        //now check that the buyer isn't placing a sale quote because of the penalty box
        sellerReg.step(state); assertEquals(sellerReg.getQuotaRemaining(0),100,.0001);
        buyerReg.step(state); assertEquals(buyerReg.getQuotaRemaining(0),100,.0001);
        //reverse the prices so that buyer would like to sell now
        when(buyerGenerator.computeLambda()).thenReturn(10d);
        when(sellerGenerator.computeLambda()).thenReturn(10d);
        orderBook.step(state);
        //they shouldn't have traded
        assertEquals(buyerReg.getQuotaRemaining(0), 100,.0001);
        assertEquals(sellerReg.getQuotaRemaining(0),100,.0001);




    }

    @Test
    public void notCrossing() throws Exception
    {


        ITQOrderBook orderBook = new ITQOrderBook(0, 0, (ask, bids,ignored1,ignore2d) -> ask, 7);
        FishState state = mock(FishState.class); when(state.getYear()).thenReturn(0);
        when(state.getRandom()).thenReturn(new MersenneTwisterFast());

        orderBook.start(state);
        orderBook.setMarkup(.1);
        orderBook.setUnitsTradedPerMatch(100);

        //first guy values quota 10: he will buy for 9 and sell for 11
        MonoQuotaRegulation buyerReg = new MonoQuotaRegulation(100);
        Fisher buyer = mock(Fisher.class);
        when(buyer.getRegulation()).thenReturn(buyerReg);
        MonoQuotaPriceGenerator buyerGenerator = mock(MonoQuotaPriceGenerator.class);
        when(buyerGenerator.computeLambda()).thenReturn(10d);
        orderBook.registerTrader(buyer,buyerGenerator);

        //the second guy is the same. No trading will occurr
        MonoQuotaRegulation sellerReg = new MonoQuotaRegulation(100);
        Fisher seller = mock(Fisher.class);
        when(seller.getRegulation()).thenReturn(sellerReg);
        MonoQuotaPriceGenerator sellerGenerator = mock(MonoQuotaPriceGenerator.class);
        when(sellerGenerator.computeLambda()).thenReturn(10d);
        orderBook.registerTrader(seller, sellerGenerator);

        orderBook.step(state);
        //no trading
        assertEquals(buyerReg.getQuotaRemaining(0), 100, .0001);
        assertEquals(sellerReg.getQuotaRemaining(0), 100, .0001);
        //no money exchange
        verify(buyer,never()).spendExogenously(anyDouble());
        verify(buyer,never()).earn(anyDouble());
        verify(seller,never()).spendExogenously(anyDouble());
        verify(seller,never()).earn(anyDouble());
        //no trading recorded
        assertEquals(orderBook.getDailyMatches(), 0, .0001);
        assertEquals(orderBook.getDailyAveragePrice(), Double.NaN, .0001);
        assertEquals(orderBook.getDailyQuotasExchanged(), 0, .0001);



    }

    //crossing but not enough quota to sell
    @Test
    public void outOfAmmo() throws Exception
    {


        ITQOrderBook orderBook = new ITQOrderBook(0, 0, (ask, bids,ignored1,ignored2) -> (ask + bids) / 2, 7);
        FishState state = mock(FishState.class); when(state.getYear()).thenReturn(0);
        when(state.getRandom()).thenReturn(new MersenneTwisterFast());
        orderBook.start(state);
        orderBook.setMarkup(.1);
        orderBook.setUnitsTradedPerMatch(100);

        //first guy values quota 100: he will buy for 90 and sell for 110
        MonoQuotaRegulation buyerReg = new MonoQuotaRegulation(100);
        Fisher buyer = mock(Fisher.class);
        when(buyer.getRegulation()).thenReturn(buyerReg);
        MonoQuotaPriceGenerator buyerGenerator = mock(MonoQuotaPriceGenerator.class);
        when(buyerGenerator.computeLambda()).thenReturn(100d);
        orderBook.registerTrader(buyer,buyerGenerator);

        //the second guy values quota 10: but inPenaltyBox not enough to sell
        MonoQuotaRegulation sellerReg = new MonoQuotaRegulation(50);
        Fisher seller = mock(Fisher.class);
        when(seller.getRegulation()).thenReturn(sellerReg);
        MonoQuotaPriceGenerator sellerGenerator = mock(MonoQuotaPriceGenerator.class);
        when(sellerGenerator.computeLambda()).thenReturn(10d);
        orderBook.registerTrader(seller, sellerGenerator);

        orderBook.step(state);
        //no trading
        assertEquals(buyerReg.getQuotaRemaining(0), 100, .0001);
        assertEquals(sellerReg.getQuotaRemaining(0), 50, .0001);
        //no money exchange
        verify(buyer,never()).spendExogenously(anyDouble());
        verify(buyer,never()).earn(anyDouble());
        verify(seller,never()).spendExogenously(anyDouble());
        verify(seller,never()).earn(anyDouble());
        //no trading recorded
        assertEquals(orderBook.getDailyMatches(), 0, .0001);
        assertEquals(orderBook.getDailyAveragePrice(), Double.NaN, .0001);
        assertEquals(orderBook.getDailyQuotasExchanged(), 0, .0001);



    }





    @Test
    public void multiCrossing() throws Exception
    {


        ITQOrderBook orderBook = new ITQOrderBook(0, 0, (ask, bids,ignored1,ignore2d) -> ask, 7);
        FishState state = mock(FishState.class);
        when(state.getRandom()).thenReturn(new MersenneTwisterFast());
        orderBook.start(state);
        orderBook.setMarkup(0);
        orderBook.setUnitsTradedPerMatch(100);
        orderBook.setAllowMultipleTradesPerFisher(false);

        //create 5 people with delta 100,200,300,400,500
        MonoQuotaRegulation regs[] = new MonoQuotaRegulation[5];
        for(int i=0; i<5;i++)
        {
            regs[i] = new MonoQuotaRegulation(100);
            Fisher fisher = mock(Fisher.class);
            when(fisher.getRegulation()).thenReturn(regs[i]);
            MonoQuotaPriceGenerator buyerGenerator = mock(MonoQuotaPriceGenerator.class);
            when(buyerGenerator.computeLambda()).thenReturn((i+1) * 100d);
            orderBook.registerTrader(fisher, buyerGenerator);
        }


        orderBook.step(state);
        //some bought, some sold
        assertEquals(regs[4].getQuotaRemaining(0), 200, .0001);
        assertEquals(regs[3].getQuotaRemaining(0), 200, .0001);
        assertEquals(regs[2].getQuotaRemaining(0), 100, .0001);
        assertEquals(regs[1].getQuotaRemaining(0), 0, .0001);
        assertEquals(regs[0].getQuotaRemaining(0), 0, .0001);

        assertEquals(orderBook.getDailyMatches(), 2, .0001);
        assertEquals(orderBook.getDailyAveragePrice(), (100.01+200.01)/2, .0001); //with no markup there is a default 0.01 increase in prices so that bids and sells don't cross
        assertEquals(orderBook.getDailyQuotasExchanged(),200,.0001);




    }

    @Test
    public void multiCrossing2(){

        Log.info("If somebody places a bid, buys a bunch of quota and then is allowed to trade again in the " +
                         "same turn, he shouldn't be able to sell within the same step");

        ITQOrderBook orderBook = new ITQOrderBook(0, 0, (ask, bids,ignored1,ignore2d) -> ask, 7);
        FishState state = mock(FishState.class); when(state.getYear()).thenReturn(0);
        when(state.getRandom()).thenReturn(new MersenneTwisterFast());
        orderBook.start(state);
        orderBook.setMarkup(.1);
        orderBook.setUnitsTradedPerMatch(100);
        orderBook.setAllowMultipleTradesPerFisher(true);

        //first guy values quota 100 at first, but only 1 the second time!
        MonoQuotaRegulation buyerReg = new MonoQuotaRegulation(500);
        Fisher buyer = mock(Fisher.class);
        when(buyer.getRegulation()).thenReturn(buyerReg);
        MonoQuotaPriceGenerator buyerGenerator = mock(MonoQuotaPriceGenerator.class);
        when(buyerGenerator.computeLambda()).thenReturn(100d,1d);
        orderBook.registerTrader(buyer,buyerGenerator);

        //the second guy values quota 10: he will buy for 9 and sell for 11
        MonoQuotaRegulation sellerReg = new MonoQuotaRegulation(500);
        Fisher seller = mock(Fisher.class);
        when(seller.getRegulation()).thenReturn(sellerReg);
        MonoQuotaPriceGenerator sellerGenerator = mock(MonoQuotaPriceGenerator.class);
        when(sellerGenerator.computeLambda()).thenReturn(10d);
        orderBook.registerTrader(seller, sellerGenerator);

        orderBook.step(state);
        //they should have traded only once!
        assertEquals(buyerReg.getQuotaRemaining(0), 600,.0001);
        assertEquals(sellerReg.getQuotaRemaining(0),400,.0001);
        assertEquals(orderBook.getDailyMatches(),1,.0001);
        assertEquals(orderBook.getDailyAveragePrice(), 11, .0001);
        assertEquals(orderBook.getDailyQuotasExchanged(),100,.0001);
        //should have asked about the buyer twice though
        verify(buyerGenerator,times(2)).computeLambda();
        //they should have traded  (assuming they use seller quote)
        verify(buyer).spendExogenously( 11 * 100);
        verify(seller).earn( 11 * 100);

        assertTrue(orderBook.inPenaltyBox(buyer));
        assertTrue(!orderBook.inPenaltyBox(seller));

        //even if I step it now there isn't any trade occurring because the penalty box is there
        sellerReg.step(state); assertEquals(sellerReg.getQuotaRemaining(0),500,.0001);
        buyerReg.step(state); assertEquals(buyerReg.getQuotaRemaining(0),500,.0001);
        //reverse the prices so that buyer would like to sell now
        when(buyerGenerator.computeLambda()).thenReturn(10d);
        when(sellerGenerator.computeLambda()).thenReturn(10d);
        orderBook.step(state);
        //they shouldn't have traded
        assertEquals(buyerReg.getQuotaRemaining(0), 500,.0001);
        assertEquals(sellerReg.getQuotaRemaining(0),500,.0001);


    }

}