package uk.ac.ox.oxfish.model.market;

import uk.ac.ox.oxfish.biology.Specie;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.Counter;
import uk.ac.ox.oxfish.model.data.IntervalPolicy;
import uk.ac.ox.oxfish.model.data.DataSet;
import uk.ac.ox.oxfish.model.regs.Regulation;

/**
 * Adds data collection to the interface
 * Created by carrknight on 5/3/15.
 */
public abstract class AbstractMarket implements Market {


    public static final String LANDINGS_COLUMN_NAME = "Landings";
    public static final String EARNINGS_COLUMN_NAME = "Earnings";


    private final Counter dailyCounter = new Counter(IntervalPolicy.EVERY_DAY);

    /**
     * specie to trade in
     */
    private final Specie specie;

    private final DataSet<Market> dailyObservations = new DataSet<>(IntervalPolicy.EVERY_DAY);

    /**
     * flag to avoid starting multiple times if start is called repeatedly
     */
    private boolean started = false;

    public AbstractMarket(Specie specie) {
        this.specie = specie;
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

        //start the data-set where we are going to store the history of the counter
        dailyObservations.start(state,this);
        //the gatherers reset the counters as a side effect
        dailyObservations.registerGatherer(EARNINGS_COLUMN_NAME, market -> dailyCounter.getColumn(EARNINGS_COLUMN_NAME),
                                           Double.NaN);

        dailyObservations.registerGatherer(LANDINGS_COLUMN_NAME, market -> dailyCounter.getColumn(LANDINGS_COLUMN_NAME),
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
     * @param biomass     pounds of fish sold
     * @param fisher      the seller
     * @param regulation the regulation object the seller abides to
     * @param state       the model
     */
    @Override
    final public TradeInfo sellFish(double biomass, Fisher fisher, Regulation regulation, FishState state) {
        TradeInfo receipt = sellFishImplementation(biomass,fisher, regulation,state);
        recordTrade(receipt);
        return receipt;
    }

    /**
     * the only method to implement for subclasses. Needs to actually do the trading and return the result
     * @param biomass the biomass caught by the seller
     * @param fisher the seller
     * @param regulation the rules the seller abides to
     * @param state the model
     * @return TradeInfo  results
     */
    protected abstract TradeInfo sellFishImplementation(double biomass, Fisher fisher,
                                                        Regulation regulation, FishState state);


    public void recordTrade(TradeInfo info)
    {
        assert specie.equals(info.getSpecie());
        dailyCounter.count(EARNINGS_COLUMN_NAME,info.getMoneyExchanged());
        dailyCounter.count(LANDINGS_COLUMN_NAME, info.getBiomassTraded());

    }

    public Specie getSpecie() {
        return specie;
    }

    public DataSet<Market> getData() {
        return dailyObservations;
    }
}
