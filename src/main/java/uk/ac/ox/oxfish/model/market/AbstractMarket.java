package uk.ac.ox.oxfish.model.market;

import uk.ac.ox.oxfish.biology.Specie;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.DataGatherer;
import uk.ac.ox.oxfish.model.regs.Regulations;

import java.util.function.Function;

/**
 * Adds data collection to the interface
 * Created by carrknight on 5/3/15.
 */
public abstract class AbstractMarket implements Market {

    /**
     * money counter, reset by the dailyObservations gatherer
     */
    private double dailyEarnings = 0;

    /**
     * biomass counter, reset by the dailyObservations gatherer
     */
    private double dailyBiomassTraded = 0;

    /**
     * specie to trade in
     */
    private final Specie specie;

    private final DataGatherer<Market> dailyObservations = new DataGatherer<>(false);

    public AbstractMarket(Specie specie) {
        this.specie = specie;
    }

    @Override
    public void start(FishState state)
    {
        dailyObservations.start(state,this);
        //the gatherers reset the counters as a side effect
        dailyObservations.registerGather("MONEY_EXCHANGED", market -> {
            double money = dailyEarnings;
            dailyEarnings = 0;
            return money;
        }, Double.NaN);

        dailyObservations.registerGather("BIOMASS_TRADED", market -> {
            double biomass = dailyBiomassTraded;
            dailyBiomassTraded = 0;
            return biomass;
        },Double.NaN);





    }


    /**
     * Sells the a specific amount of fish here by calling sellFishImplementation and then store the trade result details
     *
     * @param biomass     pounds of fish sold
     * @param fisher      the seller
     * @param regulations the regulation object the seller abides to
     * @param state       the model
     */
    @Override
    final public TradeInfo sellFish(double biomass, Fisher fisher, Regulations regulations, FishState state) {
        TradeInfo receipt = sellFishImplementation(biomass,fisher,regulations,state);
        recordTrade(receipt);
        return receipt;
    }

    /**
     * the only method to implement for subclasses. Needs to actually do the trading and return the result
     * @param biomass the biomass caught by the seller
     * @param fisher the seller
     * @param regulations the rules the seller abides to
     * @param state the model
     * @return TradeInfo  results
     */
    protected abstract TradeInfo sellFishImplementation(double biomass, Fisher fisher,
                                                        Regulations regulations, FishState state);


    public void recordTrade(TradeInfo info)
    {
        assert specie.equals(info.getSpecie());
        dailyEarnings += info.getMoneyExchanged();
        dailyBiomassTraded += info.getBiomassTraded();

    }

    public Specie getSpecie() {
        return specie;
    }
}
