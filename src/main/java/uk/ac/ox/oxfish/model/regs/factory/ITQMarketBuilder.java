package uk.ac.ox.oxfish.model.regs.factory;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.market.itq.ITQOrderBook;
import uk.ac.ox.oxfish.model.market.itq.MonoQuotaPriceGenerator;

import java.util.HashMap;

/**
 * A startable useful to create an ITQ market and to create the ITQ reservation pricer.
 * Created by carrknight on 9/22/15.
 */
public class ITQMarketBuilder  implements Startable
{


    private final int specieIndex;
    /**
     * the generators of reservation prices for each fisher
     */
    private HashMap<Fisher,MonoQuotaPriceGenerator> reservationPricers = new HashMap<>();
    private ITQOrderBook market;



    public ITQMarketBuilder() {
        specieIndex = 0;
    }

    /**
     * to call only once: create the ITQ market
     *
     * @param model the model
     */
    @Override
    public void start(FishState model) {
        //create the market
        market = new ITQOrderBook(specieIndex);
        market.start(model);
        //gather market data
        model.getDailyDataSet().registerGatherer("ITQ Trades", state1 -> market.getDailyMatches(),
                                                 Double.NaN);
        model.getDailyDataSet().registerGatherer("ITQ Prices", state1 -> market.getDailyAveragePrice(),
                                                 Double.NaN);
        model.getDailyDataSet().registerGatherer("ITQ Last Closing Price", state1 -> market.getLastClosingPrice(),
                                                 Double.NaN);

        //and give to each fisher a price-maker
        for(Fisher fisher : model.getFishers())
        {
            MonoQuotaPriceGenerator reservationPricer = new MonoQuotaPriceGenerator(specieIndex, false);
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
        for(MonoQuotaPriceGenerator pricer : reservationPricers.values())
            pricer.turnOff();
    }


    public MonoQuotaPriceGenerator getReservationPriceGenerator(Fisher fisher)
    {
        return reservationPricers.get(fisher);
    }

    public ITQOrderBook getMarket() {
        return market;
    }
}
