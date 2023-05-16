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

package uk.ac.ox.oxfish.model.market.itq;

import ec.util.MersenneTwisterFast;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.data.collectors.Counter;
import uk.ac.ox.oxfish.model.data.collectors.IntervalPolicy;
import uk.ac.ox.oxfish.model.regs.QuotaPerSpecieRegulation;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.util.*;
import java.util.logging.Logger;

/**
 * An order book to trade ITQs. Very experimental. For now allows only one trade per person per step
 * Created by carrknight on 8/20/15.
 */
public class ITQOrderBook implements Steppable, Startable {


    public static final String MATCHES_COLUMN_NAME = "MATCHES";
    public static final String QUOTA_COLUMN_NAME = "QUOTA_VOLUME";
    public static final String MONEY_COLUMN_NAME = "MONEY_VOLUME";
    private final int yearOfImplementation;
    private final int specieIndex;
    /**
     * here we put agents who bought to prevent them from selling immediately after and avoid
     * bid-ask bounces
     */
    private final PenaltyBox penaltyBox;
    private final LinkedList<Fisher> toPenalize = new LinkedList<>();
    private final PriorityQueue<Quote> asks;
    private final PriorityQueue<Quote> bids;
    private final Counter counter = new Counter(IntervalPolicy.EVERY_DAY);
    HashMap<Fisher, PriceGenerator> pricers = new HashMap<>();
    private double markup = 0.05;
    private double lastClosingPrice = Double.NaN;
    private PricingPolicy pricingPolicy;
    private int unitsTradedPerMatch = 100;
    private boolean allowMultipleTradesPerFisher = false;
    private Stoppable stoppable;


    /**
     * create the order book
     *
     * @param specieIndex        the index of the species being traded
     * @param implementationYear the year this order book starts asking for quotes
     * @param pricingPolicy
     * @param penaltyDuration
     */
    public ITQOrderBook(
        final int specieIndex, final int implementationYear,
        final PricingPolicy pricingPolicy, final int penaltyDuration
    ) {

        //create the queues holding on to the quotes
        asks = new PriorityQueue<>(100, new Comparator<Quote>() {
            @Override
            public int compare(final Quote quote, final Quote o) {
                return quote.compareTo(o);
            }
        });
        bids = new PriorityQueue<>(100, new Comparator<Quote>() {
            @Override
            public int compare(final Quote o1, final Quote o2) {
                return -o1.compareTo(o2);
            }
        });
        this.specieIndex = specieIndex;
        this.yearOfImplementation = implementationYear;
        this.pricingPolicy = pricingPolicy;
        this.penaltyBox = new PenaltyBox(penaltyDuration);
    }

    /**
     * this gets called by the fish-state right after the scenario has started. It's useful to set up steppables
     * or just to percolate a reference to the model
     *
     * @param model the model
     */
    @Override
    public void start(final FishState model) {
        counter.addColumn(MATCHES_COLUMN_NAME);
        counter.addColumn(QUOTA_COLUMN_NAME);
        counter.addColumn(MONEY_COLUMN_NAME);
        stoppable = model.scheduleEveryDay(this, StepOrder.POLICY_UPDATE);
        counter.start(model);
    }

    /**
     * tell the startable to turnoff,
     */
    @Override
    public void turnOff() {
        if (stoppable != null)
            stoppable.stop();
    }

    public void registerTrader(final Fisher fisher, final PriceGenerator pricer) {
        pricers.put(fisher, pricer);
    }


    public void step(final SimState state) {


        penaltyBox.step(state); //tell the penalty box to update durations
        final MersenneTwisterFast random = ((FishState) state).getRandom();
        final List<Map.Entry<Fisher, PriceGenerator>> traders = new ArrayList<>(pricers.entrySet());
        //sort from hash before shuffling or the randomization might not be the same even if the seed is left constant
        traders.sort((o1, o2) -> Integer.compare(o1.getKey().getID(), o2.getKey().getID()));
        Collections.shuffle(traders, new Random(random.nextLong()));


        if (((FishState) state).getYear() >= yearOfImplementation) {
            //fill the quotes
            for (final Map.Entry<Fisher, PriceGenerator> trader : traders) {
                generatePricesAndPutOnBook(trader.getKey(), trader.getValue(), true, true);
            }

            Logger.getGlobal().fine(specieIndex + " ask size : " + asks.size() + " ---- " + bids.size());
            Logger.getGlobal().fine(specieIndex + " asks: " + asks);
            Logger.getGlobal().fine(specieIndex + " bids: " + bids);

            //go for it
            clearQuotes();


            //clear the quotes
            asks.clear();
            bids.clear();

            //add buyers to penalty box
            for (final Fisher trader : toPenalize)
                penaltyBox.registerTrader(trader);
            toPenalize.clear();
        }


    }

    public void generatePricesAndPutOnBook(
        final Fisher fisher, final PriceGenerator priceGenerator,
        final boolean generateAsk, final boolean generateBid
    ) {
        final double price = priceGenerator.computeLambda();
        if (Double.isFinite(price)) {
            final double buyPrice = FishStateUtilities.round(price * (1 - markup));
            //do I want to buy?
            if (generateBid && price > 0) {
                bids.add(new Quote(
                    buyPrice,
                    fisher
                ));
            }
            //can I sell?
            if (generateAsk &&
                ((QuotaPerSpecieRegulation) fisher.getRegulation()).getQuotaRemaining(
                    specieIndex) >= unitsTradedPerMatch &&
                !penaltyBox.has(fisher)) {
                final double salePrice = Math.max(
                    FishStateUtilities.round(Math.max(price * (1 + markup), markup)),
                    buyPrice + FishStateUtilities.EPSILON
                ) //never let bids and ask cross, even if markup is 0!
                    ;
                assert buyPrice < salePrice;
                asks.add(new Quote(
                    salePrice,
                    fisher
                ));
            }
        }
    }

    private void clearQuotes() {
        //this would work well in recursion but unfortunately if the quantity traded is very small and many traders
        //it will go on stackoverflow
        while (true) {
            if (bids.isEmpty() || asks.isEmpty())
                return;

            final Quote bestBid = bids.remove();
            final Quote bestAsk = asks.remove();
            final Fisher seller = bestAsk.getTrader();
            final Fisher buyer = bestBid.getTrader();

            assert !penaltyBox.has(seller); //seller ought not to be in the penalty box
            //does somebody want to trade?
            if (bestAsk.getPrice() <= bestBid.getPrice()) {

                final Quote secondBestAsk = asks.peek();
                final Quote secondBestBid = bids.peek();
                final double tradingPrice = pricingPolicy.tradePrice(bestAsk.getPrice(), bestBid.getPrice(),
                    secondBestAsk != null ? secondBestAsk.getPrice() : Double.NaN,
                    secondBestBid != null ? secondBestBid.getPrice() : Double.NaN
                );
                Logger.getGlobal().fine(bestAsk.getPrice() + " , bid: " + bestBid.getPrice() + " ,secondBestAsk " +
                    (secondBestAsk != null ? secondBestAsk.getPrice() : Double.NaN) + " , secondBestBid:  " +
                    (secondBestBid != null ? secondBestBid.getPrice() : Double.NaN) + " ---->" +
                    tradingPrice
                );
                assert tradingPrice >= bestAsk.getPrice();
                assert tradingPrice <= bestBid.getPrice();

                //now trade!

                final QuotaPerSpecieRegulation buyerQuota = (QuotaPerSpecieRegulation) buyer.getRegulation();
                final QuotaPerSpecieRegulation sellerQuota = (QuotaPerSpecieRegulation) seller.getRegulation();


                buyerQuota.setQuotaRemaining(
                    specieIndex,
                    buyerQuota.getQuotaRemaining(specieIndex) + unitsTradedPerMatch
                );
                sellerQuota.setQuotaRemaining(
                    specieIndex,
                    sellerQuota.getQuotaRemaining(specieIndex) - unitsTradedPerMatch
                );

                buyer.spendExogenously(unitsTradedPerMatch * tradingPrice);
                seller.earn(unitsTradedPerMatch * tradingPrice);
                counter.count(QUOTA_COLUMN_NAME, unitsTradedPerMatch);
                counter.count(MONEY_COLUMN_NAME, unitsTradedPerMatch * tradingPrice);
                counter.count(MATCHES_COLUMN_NAME, 1);


                Logger.getGlobal().fine(() ->
                    buyer + " bought " + unitsTradedPerMatch + " quotas of species " + specieIndex + " from " + seller);
                Logger.getGlobal().fine(() -> buyer + " now has " + buyerQuota.getQuotaRemaining(specieIndex) +
                    " quotas left while" + seller + " has " +
                    sellerQuota.getQuotaRemaining(specieIndex));

                toPenalize.add(buyer);
                lastClosingPrice = tradingPrice;
                assert sellerQuota.getQuotaRemaining(specieIndex) >= 0;


                //if you allow it, let buyer and seller posts more offers
                if (allowMultipleTradesPerFisher) {
                    generatePricesAndPutOnBook(buyer, pricers.get(buyer), false, true);
                    generatePricesAndPutOnBook(seller, pricers.get(seller), true, false);
                }

                //again!
            } else {
                return;
            }

        }
    }


    public double getMarkup() {
        return markup;
    }

    public void setMarkup(final double markup) {
        this.markup = markup;
    }

    public int getUnitsTradedPerMatch() {
        return unitsTradedPerMatch;
    }

    public void setUnitsTradedPerMatch(final int unitsTradedPerMatch) {
        this.unitsTradedPerMatch = unitsTradedPerMatch;
    }


    /**
     * How many buyers and sellers fruitfully since the beginning of the day
     */
    public double getDailyMatches() {
        return counter.getColumn(MATCHES_COLUMN_NAME);
    }

    public double getDailyTradeValue() {
        return counter.getColumn(MONEY_COLUMN_NAME);
    }

    /**
     * How many buyers and sellers traded since the beginning of the day
     */
    public double getDailyAveragePrice() {
        final double quotas = getDailyQuotasExchanged();
        if (quotas == 0)
            return Double.NaN;
        return counter.getColumn(MONEY_COLUMN_NAME) / quotas;
    }

    /**
     * How many buyers and sellers fruitfully since the beginning of the day
     */
    public double getDailyQuotasExchanged() {
        return counter.getColumn(QUOTA_COLUMN_NAME);
    }

    public double getLastClosingPrice() {
        return lastClosingPrice;
    }

    public int getYearOfImplementation() {
        return yearOfImplementation;
    }

    public PricingPolicy getPricingPolicy() {
        return pricingPolicy;
    }

    public void setPricingPolicy(final PricingPolicy pricingPolicy) {
        this.pricingPolicy = pricingPolicy;
    }


    /**
     * check if the trader is in the penalty box!
     *
     * @param fisher
     * @return
     */
    public boolean inPenaltyBox(final Fisher fisher) {
        return penaltyBox.has(fisher);
    }

    public boolean isAllowMultipleTradesPerFisher() {
        return allowMultipleTradesPerFisher;
    }

    public void setAllowMultipleTradesPerFisher(final boolean allowMultipleTradesPerFisher) {
        this.allowMultipleTradesPerFisher = allowMultipleTradesPerFisher;
    }
}
