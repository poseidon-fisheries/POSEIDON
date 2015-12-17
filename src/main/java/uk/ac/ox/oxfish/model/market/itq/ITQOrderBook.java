package uk.ac.ox.oxfish.model.market.itq;

import com.esotericsoftware.minlog.Log;
import ec.util.MersenneTwisterFast;
import sim.engine.SimState;
import sim.engine.Steppable;
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

    private Queue<Quote> asks;

    private Queue<Quote> bids;

    private double markup = 0.05;

    private final int yearOfImplementation;

    private double lastClosingPrice = Double.NaN;

    private Counter counter = new Counter(IntervalPolicy.EVERY_DAY);

    private PricingPolicy pricingPolicy;

    private int unitsTradedPerMatch = 100;


    private final int specieIndex;

    /**
     * here we put agents who bought to prevent them from selling immediately after and avoid
     * bid-ask bounces
     */
    private final PenaltyBox penaltyBox;


    private final LinkedList<Fisher> toPenalize = new LinkedList<>();


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
        model.scheduleEveryDay(this, StepOrder.POLICY_UPDATE);
        counter.start(model);
    }

    /**
     * tell the startable to turnoff,
     */
    @Override
    public void turnOff() {

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
        asks = new PriorityQueue<>(100, Quote::compareTo);
        bids = new PriorityQueue<>(100, (o1, o2) -> -o1.compareTo(o2));
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
        Collections.shuffle(traders,new Random(random.nextLong()));

        if(((FishState) state).getYear() >= yearOfImplementation) {
            //fill the quotes
            for (Map.Entry<Fisher, PriceGenerator> trader : traders) {
                double price = trader.getValue().computeLambda();
                if (Double.isFinite(price)) {
                    double buyPrice = FishStateUtilities.round(price * (1 - markup));
                    //do I want to buy?
                    if (price > 0) {
                        bids.add(new Quote(
                                buyPrice,
                                trader.getKey()));
                    }
                    //can I sell?
                    if (((QuotaPerSpecieRegulation) trader.getKey().getRegulation()).getQuotaRemaining(
                            specieIndex) >= unitsTradedPerMatch &&
                            !penaltyBox.has(trader.getKey())) {
                        double salePrice = Math.max(FishStateUtilities.round(Math.max(price * (1 + markup), .5)),
                                                    buyPrice + FishStateUtilities.EPSILON) //never let bids and ask cross, even if markup is 0!
                                ;
                        assert buyPrice < salePrice;
                        asks.add(new Quote(
                                salePrice,
                                trader.getKey()));
                    }
                }
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

    private void clearQuotes()
    {
        if(bids.isEmpty() || asks.isEmpty())
            return;

        Quote bestBid = bids.remove();
        Quote bestAsk = asks.remove();
        assert !penaltyBox.has(bestAsk.getTrader()); //seller ought not to be in the penalty box
        //does somebody want to trade?
        if (bestAsk.getPrice() <= bestBid.getPrice()) {

            Quote secondBestAsk = asks.peek();
            Quote secondBestBid = bids.peek();
            double tradingPrice = pricingPolicy.tradePrice(bestAsk.getPrice(),bestBid.getPrice(),
                                                           secondBestAsk != null ? secondBestAsk.getPrice() : Double.NaN,
                                                           secondBestBid != null ? secondBestBid.getPrice() : Double.NaN
            );
            if(Log.TRACE)
                Log.trace( bestAsk.getPrice() + " , bid: " + bestBid.getPrice() + " ,secondBestAsk " +
                                   (secondBestAsk != null ? secondBestAsk.getPrice() : Double.NaN)  + " , secondBestBid:  " +
                                   (secondBestBid != null ? secondBestBid.getPrice() : Double.NaN) + " ---->" +
                        tradingPrice
                );
            assert tradingPrice >= bestAsk.getPrice();
            assert tradingPrice <=bestBid.getPrice();

            //now trade!
            QuotaPerSpecieRegulation buyerQuota = (QuotaPerSpecieRegulation) bestBid.getTrader().getRegulation();
            QuotaPerSpecieRegulation sellerQuota = (QuotaPerSpecieRegulation) bestAsk.getTrader().getRegulation();

            buyerQuota.setQuotaRemaining(specieIndex, buyerQuota.getQuotaRemaining(specieIndex)+unitsTradedPerMatch);
            sellerQuota.setQuotaRemaining(specieIndex, sellerQuota.getQuotaRemaining(specieIndex)-unitsTradedPerMatch);
            bestBid.getTrader().spendExogenously(unitsTradedPerMatch * tradingPrice);
            bestAsk.getTrader().earn(unitsTradedPerMatch * tradingPrice);
            counter.count(QUOTA_COLUMN_NAME, unitsTradedPerMatch);
            counter.count(MONEY_COLUMN_NAME, unitsTradedPerMatch * tradingPrice);
            counter.count(MATCHES_COLUMN_NAME,1);


            toPenalize.add(bestBid.getTrader());
            lastClosingPrice = tradingPrice;
            assert sellerQuota.getQuotaRemaining(specieIndex)>=0;

            //again!
            clearQuotes();

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
}
