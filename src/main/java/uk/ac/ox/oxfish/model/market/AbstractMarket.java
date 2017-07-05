package uk.ac.ox.oxfish.model.market;

import com.esotericsoftware.minlog.Log;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Hold;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.Gatherer;
import uk.ac.ox.oxfish.model.data.collectors.Counter;
import uk.ac.ox.oxfish.model.data.collectors.IntervalPolicy;
import uk.ac.ox.oxfish.model.data.collectors.TimeSeries;
import uk.ac.ox.oxfish.model.regs.Regulation;

/**
 * Adds data collection to the interface
 * Created by carrknight on 5/3/15.
 */
public abstract class AbstractMarket implements Market {


    public static final String LANDINGS_COLUMN_NAME = "Landings";
    public static final String EARNINGS_COLUMN_NAME = "Earnings";
    public static final String PRICE_COLUMN_NAME = "Marginal Price";


    private final Counter dailyCounter = new Counter(IntervalPolicy.EVERY_DAY);


    private final TimeSeries<Market> dailyObservations = new TimeSeries<>(IntervalPolicy.EVERY_DAY);

    /**
     * flag to avoid starting multiple times if start is called repeatedly
     */
    private boolean started = false;

    public AbstractMarket() {

    }

    /**
     * starts gathering data. If called multiple times all the calls after the first are ignored
     * @param state the model
     */
    @Override
    public void start(FishState state)
    {
        if(started) //don't start twice
            return;

        //start the counter
        dailyCounter.start(state);
        dailyCounter.addColumn(EARNINGS_COLUMN_NAME);
        dailyCounter.addColumn(LANDINGS_COLUMN_NAME);
        dailyCounter.addColumn(PRICE_COLUMN_NAME);

        //start the data-set where we are going to store the history of the counter
        dailyObservations.start(state,this);
        //the gatherers reset the counters as a side effect
        dailyObservations.registerGatherer(EARNINGS_COLUMN_NAME, new Gatherer<Market>() {
                                               @Override
                                               public Double apply(Market market) {
                                                   return dailyCounter.getColumn(EARNINGS_COLUMN_NAME);
                                               }
                                           },
                                           Double.NaN);

        dailyObservations.registerGatherer(LANDINGS_COLUMN_NAME, new Gatherer<Market>() {
                                               @Override
                                               public Double apply(Market market) {
                                                   return dailyCounter.getColumn(LANDINGS_COLUMN_NAME);
                                               }
                                           },
                                           Double.NaN);

        dailyObservations.registerGatherer(PRICE_COLUMN_NAME, Market::getMarginalPrice,
                                           Double.NaN);

        started = true;

    }

    /**
     * tell the startable to turnoff,
     */
    @Override
    public void turnOff() {
        dailyCounter.turnOff();
        dailyObservations.turnOff();
    }

    /**
     * Sells the a specific amount of fish here by calling sellFishImplementation and then store the trade result details
     *
     * @param hold
     * @param fisher      the seller
     * @param regulation the regulation object the seller abides to
     * @param state       the model
     */
    @Override
    final public TradeInfo sellFish(
            Hold hold, Fisher fisher, Regulation regulation,
            FishState state, Species species) {
        TradeInfo receipt = sellFishImplementation(
                hold,
                fisher, regulation, state, species);
        recordTrade(receipt);
        return receipt;
    }

    protected abstract TradeInfo sellFishImplementation(
            Hold hold, Fisher fisher, Regulation regulation, FishState state, Species species);


    public void recordTrade(TradeInfo info)
    {
        if(Log.TRACE && info.getBiomassTraded() >  0)
            Log.trace("recorded the following trade: " + info);
        dailyCounter.count(EARNINGS_COLUMN_NAME,info.getMoneyExchanged());
        dailyCounter.count(LANDINGS_COLUMN_NAME, info.getBiomassTraded());



    }


    public TimeSeries<Market> getData() {
        return dailyObservations;
    }
}
