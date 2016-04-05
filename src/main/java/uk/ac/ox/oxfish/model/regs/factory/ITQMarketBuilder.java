package uk.ac.ox.oxfish.model.regs.factory;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.market.itq.ITQOrderBook;
import uk.ac.ox.oxfish.model.market.itq.MonoQuotaPriceGenerator;
import uk.ac.ox.oxfish.model.market.itq.PriceGenerator;

import java.util.HashMap;
import java.util.function.Supplier;

/**
 * A startable useful to create an ITQ market and to create the ITQ reservation pricer.
 * Created by carrknight on 9/22/15.
 */
public class ITQMarketBuilder  implements Startable
{


    private final int speciesIndex;
    /**
     * the generators of reservation prices for each fisher
     */
    private HashMap<Fisher,PriceGenerator> reservationPricers = new HashMap<>();


    final private Supplier<PriceGenerator> priceGeneratorMaker;

    private final ITQOrderBook market;


    public ITQMarketBuilder(
            int speciesIndex,
            Supplier<PriceGenerator> priceGeneratorMaker) {
        this.speciesIndex = speciesIndex;
        this.priceGeneratorMaker = priceGeneratorMaker;
        market = new ITQOrderBook(speciesIndex, 1,
                                  (askPrice, bidPrice, secondBestAsk, secondBestBid) -> askPrice, 7);

    }

    /**
     * creates an ITQ market using MonoQuotaPriceGenerator
     * @param speciesIndex
     */
    public ITQMarketBuilder(int speciesIndex) {

        this(speciesIndex,() -> new MonoQuotaPriceGenerator(speciesIndex,false));
    }

    /**
     * to call only once: create the ITQ market
     *
     * @param model the model
     */
    @Override
    public void start(FishState model) {
        //create the market
        market.start(model);
        String speciesName = model.getSpecies().get(speciesIndex).getName();
        //gather market data
        model.getDailyDataSet().registerGatherer("ITQ Trades Of " + speciesName, state1 -> market.getDailyMatches(),
                                                 Double.NaN);
        model.getDailyDataSet().registerGatherer("ITQ Prices Of " + speciesName, state1 -> market.getDailyAveragePrice(),
                                                 Double.NaN);
        model.getDailyDataSet().registerGatherer("ITQ Last Closing Price Of " + speciesName, state1 -> market.getLastClosingPrice(),
                                                 Double.NaN);

        //and give to each fisher a price-maker
        for(Fisher fisher : model.getFishers())
        {
            PriceGenerator reservationPricer = priceGeneratorMaker.get();
            reservationPricer.start(model, fisher);
            market.registerTrader(fisher, reservationPricer);
            //record it
            reservationPricers.put(fisher,reservationPricer);


        }


    }

    /**
     * tell the startable to turnoff,
     */
    @Override
    public void turnOff() {
        market.turnOff();
        for(PriceGenerator pricer : reservationPricers.values())
            pricer.turnOff();
    }


    public PriceGenerator getReservationPriceGenerator(Fisher fisher)
    {
        return reservationPricers.get(fisher);
    }

    public ITQOrderBook getMarket() {
        return market;
    }
}
