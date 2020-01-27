package uk.ac.ox.oxfish.model.market;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Hold;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.Gatherer;
import uk.ac.ox.oxfish.model.data.collectors.IntervalPolicy;
import uk.ac.ox.oxfish.model.data.collectors.TimeSeries;
import uk.ac.ox.oxfish.model.regs.Regulation;

import java.util.function.Predicate;

import static uk.ac.ox.oxfish.model.market.AbstractMarket.*;

/**
 * There are two markets, you have access to the default one unless you pass a predicate at which point you are
 */
public class ConditionalMarket implements Market {


    final private AbstractMarket defaultMarket;

    final private AbstractMarket passThePredicateMarket;

    /**
     * when true, the fisher has access to the passThePredicateMarket
     * when false, the fisher has access to the other one
     */
    final private Predicate<Fisher> marketChecker;

    private final TimeSeries<Market> dailyObservations = new TimeSeries<>(IntervalPolicy.EVERY_DAY);

    private boolean started;


    public ConditionalMarket(AbstractMarket defaultMarket,
                             AbstractMarket passThePredicateMarket,
                             Predicate<Fisher> marketChecker) {
        this.defaultMarket = defaultMarket;
        this.passThePredicateMarket = passThePredicateMarket;
        this.marketChecker = marketChecker;
    }

    @Override
    public void start(FishState model) {

        Preconditions.checkArgument(defaultMarket.getSpecies()!=null,
                " market doesn't know the species to trade in");
        if(started) //don't start twice
            return;

        defaultMarket.start(model);
        passThePredicateMarket.start(model);

        //start the data-set where we are going to store the history of the counter
        dailyObservations.start(model,this);
        //the gatherers reset the counters as a side effect
        dailyObservations.registerGatherer(EARNINGS_COLUMN_NAME, new Gatherer<Market>() {
                    @Override
                    public Double apply(Market market) {
                        return defaultMarket.getDailyCounter().getColumn(EARNINGS_COLUMN_NAME) +
                                passThePredicateMarket.getDailyCounter().getColumn(EARNINGS_COLUMN_NAME)
                                ;
                    }
                },
                Double.NaN);

        dailyObservations.registerGatherer(LANDINGS_COLUMN_NAME, new Gatherer<Market>() {
                    @Override
                    public Double apply(Market market) {
                        return defaultMarket.getDailyCounter().getColumn(LANDINGS_COLUMN_NAME) +
                                passThePredicateMarket.getDailyCounter().getColumn(LANDINGS_COLUMN_NAME)
                                ;
                    }
                },
                Double.NaN);

        dailyObservations.registerGatherer(PRICE_COLUMN_NAME, Market::getMarginalPrice,
                Double.NaN);

        started = true;


    }

    @Override
    public double getMarginalPrice() {
        return defaultMarket.getMarginalPrice();
    }

    @Override
    public Species getSpecies() {
        assert defaultMarket.getSpecies().equals(passThePredicateMarket.getSpecies());
        return defaultMarket.getSpecies();
    }

    @Override
    public void setSpecies(Species species) {
        defaultMarket.setSpecies(species);
        passThePredicateMarket.setSpecies(species);
    }


    @Override
    public TradeInfo sellFish(Hold hold, Fisher fisher, Regulation regulation, FishState state, Species species) {
        if(marketChecker.test(fisher))
           return passThePredicateMarket.sellFish(hold, fisher, regulation, state, species);
        else
            return defaultMarket.sellFish(hold, fisher, regulation, state, species);


    }

    @Override
    public TimeSeries<Market> getData() {
        return dailyObservations;
    }

    @Override
    public boolean isStarted() {
        return started;
    }

    @Override
    public void turnOff() {
        defaultMarket.turnOff();
        passThePredicateMarket.turnOff();
    }
}
