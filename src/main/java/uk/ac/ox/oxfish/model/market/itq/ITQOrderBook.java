package uk.ac.ox.oxfish.model.market.itq;

import com.esotericsoftware.minlog.Log;
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

/**
 * An order book to trade ITQs. Very experimental. For now allows only one trade per person per step
 * Created by carrknight on 8/20/15.
 */
public class ITQOrderBook implements Steppable,Startable{


    public static final String MATCHES_COLUMN_NAME = "MATCHES";
    public static final String QUOTA_COLUMN_NAME = "QUOTA_VOLUME";
    public static final String MONEY_COLUMN_NAME = "MONEY_VOLUME";
    HashMap<Fisher,PriceGenerator> pricers  = new HashMap<>();

    private PriorityQueue<Quote> asks;

    private PriorityQueue<Quote> bids;

    private double markup = 0.05;

    private final int yearOfImplementation;

    private double lastClosingPrice = Double.NaN;

    private Counter counter = new Counter(IntervalPolicy.EVERY_DAY);

    private PricingPolicy pricingPolicy;

    private int unitsTradedPerMatch = 100;


    private final int specieIndex;

    private boolean allowMultipleTradesPerFisher = false;

    /**
     * here we put agents who bought to prevent them from selling immediately after and avoid
     * bid-ask bounces
     */
    private final PenaltyBox penaltyBox;


    private final LinkedList<Fisher> toPenalize = new LinkedList<>();
    private Stoppable stoppable;


    /**
     * this gets called by the fish-state right after the scenario has started. It's useful to set up steppables
     * or just to percolate a reference to the model
     *
     * @param model the model
     */
    @Override
    public void start(FishState model) {
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
        if(stoppable!=null)
            stoppable.stop();
    }

    /**
     * create the order book
     * @param specieIndex the index of the species being traded
     * @param implementationYear the year this order book starts asking for quotes
     * @param pricingPolicy
     * @param penaltyDuration
     */
    public ITQOrderBook(
            int specieIndex, int implementationYear,
            PricingPolicy pricingPolicy, int penaltyDuration)
    {

        //create the queues holding on to the quotes
        asks = new PriorityQueue<>(100, new Comparator<Quote>() {
            @Override
            public int compare(Quote quote, Quote o) {
                return quote.compareTo(o);
            }
        });
        bids = new PriorityQueue<>(100, new Comparator<Quote>() {
            @Override
            public int compare(Quote o1, Quote o2) {
                return -o1.compareTo(o2);
            }
        });
        this.specieIndex = specieIndex;
        this.yearOfImplementation = implementationYear;
        this.pricingPolicy = pricingPolicy;
        this.penaltyBox = new PenaltyBox(penaltyDuration);
    }


    public void registerTrader(Fisher fisher, PriceGenerator pricer)
    {
        pricers.put(fisher,pricer);
    }


    public void step(SimState state)
    {


        penaltyBox.step(state); //tell the penalty box to update durations
        MersenneTwisterFast random = ((FishState) state).getRandom();
        List<Map.Entry<Fisher,PriceGenerator>> traders = new ArrayList<>(pricers.entrySet());
        //sort from hash before shuffling or the randomization might not be the same even if the seed is left constant
        traders.sort((o1, o2) -> Integer.compare(o1.getKey().getID(), o2.getKey().getID()));
        Collections.shuffle(traders,new Random(random.nextLong()));


        if(((FishState) state).getYear() >= yearOfImplementation) {
            //fill the quotes
            for (Map.Entry<Fisher, PriceGenerator> trader : traders) {
                generatePricesAndPutOnBook(trader.getKey(), trader.getValue(),true,true);
            }

            if (Log.TRACE) {
                Log.trace(specieIndex + " ask size : " + asks.size() + " ---- " + bids.size());
                Log.trace(specieIndex + " asks: " + asks.toString());
                Log.trace(specieIndex + " bids: " + bids.toString());


            }


            //go for it
            clearQuotes();


            //clear the quotes
            asks.clear();
            bids.clear();

            //add buyers to penalty box
            for(Fisher trader : toPenalize)
                penaltyBox.registerTrader(trader);
            toPenalize.clear();
        }



    }

    public void generatePricesAndPutOnBook(Fisher fisher, PriceGenerator priceGenerator,
                                           boolean generateAsk, boolean generateBid) {
        double price =  priceGenerator.computeLambda();
        if (Double.isFinite(price)) {
            double buyPrice = FishStateUtilities.round(price * (1 - markup));
            //do I want to buy?
            if (generateBid && price > 0) {
                bids.add(new Quote(
                        buyPrice,
                        fisher));
            }
            //can I sell?
            if ( generateAsk  &&
                    ((QuotaPerSpecieRegulation) fisher.getRegulation()).getQuotaRemaining(
                            specieIndex) >= unitsTradedPerMatch &&
                    !penaltyBox.has(fisher)) {
                double salePrice = Math.max(FishStateUtilities.round(Math.max(price * (1 + markup), .5)),
                                            buyPrice + FishStateUtilities.EPSILON) //never let bids and ask cross, even if markup is 0!
                        ;
                assert buyPrice < salePrice;
                asks.add(new Quote(
                        salePrice,
                        fisher));
            }
        }
    }

    private void clearQuotes()
    {
        //this would work well in recursion but unfortunately if the quantity traded is very small and many traders
        //it will go on stackoverflow
        while(true)
        {
            if(bids.isEmpty() || asks.isEmpty())
                return;

            Quote bestBid = bids.remove();
            Quote bestAsk = asks.remove();
            Fisher seller = bestAsk.getTrader();
            Fisher buyer = bestBid.getTrader();

            assert !penaltyBox.has(seller); //seller ought not to be in the penalty box
            //does somebody want to trade?
            if (bestAsk.getPrice() <= bestBid.getPrice()) {

                Quote secondBestAsk = asks.peek();
                Quote secondBestBid = bids.peek();
                double tradingPrice = pricingPolicy.tradePrice(bestAsk.getPrice(), bestBid.getPrice(),
                                                               secondBestAsk != null ? secondBestAsk.getPrice() : Double.NaN,
                                                               secondBestBid != null ? secondBestBid.getPrice() : Double.NaN
                );
                if (Log.TRACE)
                    Log.trace(bestAsk.getPrice() + " , bid: " + bestBid.getPrice() + " ,secondBestAsk " +
                                      (secondBestAsk != null ? secondBestAsk.getPrice() : Double.NaN) + " , secondBestBid:  " +
                                      (secondBestBid != null ? secondBestBid.getPrice() : Double.NaN) + " ---->" +
                                      tradingPrice
                    );
                assert tradingPrice >= bestAsk.getPrice();
                assert tradingPrice <= bestBid.getPrice();

                //now trade!

                QuotaPerSpecieRegulation buyerQuota = (QuotaPerSpecieRegulation) buyer.getRegulation();
                QuotaPerSpecieRegulation sellerQuota = (QuotaPerSpecieRegulation) seller.getRegulation();


                buyerQuota.setQuotaRemaining(specieIndex, buyerQuota.getQuotaRemaining(specieIndex) + unitsTradedPerMatch);
                sellerQuota.setQuotaRemaining(specieIndex,sellerQuota.getQuotaRemaining(specieIndex) - unitsTradedPerMatch);

                buyer.spendExogenously(unitsTradedPerMatch * tradingPrice);
                seller.earn(unitsTradedPerMatch * tradingPrice);
                counter.count(QUOTA_COLUMN_NAME, unitsTradedPerMatch);
                counter.count(MONEY_COLUMN_NAME, unitsTradedPerMatch * tradingPrice);
                counter.count(MATCHES_COLUMN_NAME, 1);


                if (Log.TRACE) {
                    Log.trace(
                            buyer + " bought " + unitsTradedPerMatch + " quotas of species " + specieIndex + " from " + seller);
                    Log.trace(buyer + " now has " + buyerQuota.getQuotaRemaining(specieIndex) +
                                      " quotas left while" + seller + " has " +
                                      sellerQuota.getQuotaRemaining(specieIndex));
                }

                toPenalize.add(buyer);
                lastClosingPrice = tradingPrice;
                assert sellerQuota.getQuotaRemaining(specieIndex) >= 0;


                //if you allow it, let buyer and seller posts more offers
                if (allowMultipleTradesPerFisher) {
                    generatePricesAndPutOnBook(buyer, pricers.get(buyer), false, true);
                    generatePricesAndPutOnBook(seller, pricers.get(seller), true, false);
                }

                //again!
            }
            else {
                return;
            }

        }
    }


    public double getMarkup() {
        return markup;
    }

    public void setMarkup(double markup) {
        this.markup = markup;
    }

    public int getUnitsTradedPerMatch() {
        return unitsTradedPerMatch;
    }

    public void setUnitsTradedPerMatch(int unitsTradedPerMatch) {
        this.unitsTradedPerMatch = unitsTradedPerMatch;
    }


    /**
     * How many buyers and sellers fruitfully since the beginning of the day
     */
    public double getDailyMatches()
    {
        return counter.getColumn(MATCHES_COLUMN_NAME);
    }

    /**
     * How many buyers and sellers fruitfully since the beginning of the day
     */
    public double getDailyQuotasExchanged()
    {
        return counter.getColumn(QUOTA_COLUMN_NAME);
    }

    public double getDailyTradeValue(){
        return counter.getColumn(MONEY_COLUMN_NAME);
    }

    /**
     * How many buyers and sellers traded since the beginning of the day
     */
    public double getDailyAveragePrice()
    {
        double quotas = getDailyQuotasExchanged();
        if(quotas ==0)
            return Double.NaN;
        return counter.getColumn(MONEY_COLUMN_NAME)/ quotas;
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

    public void setPricingPolicy(PricingPolicy pricingPolicy) {
        this.pricingPolicy = pricingPolicy;
    }


    /**
     * check if the trader is in the penalty box!
     * @param fisher
     * @return
     */
    public boolean inPenaltyBox(Fisher fisher) {
        return penaltyBox.has(fisher);
    }

    public boolean isAllowMultipleTradesPerFisher() {
        return allowMultipleTradesPerFisher;
    }

    public void setAllowMultipleTradesPerFisher(boolean allowMultipleTradesPerFisher) {
        this.allowMultipleTradesPerFisher = allowMultipleTradesPerFisher;
    }
}
