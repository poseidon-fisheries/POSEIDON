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

package uk.ac.ox.oxfish.model.regs.factory;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.data.Gatherer;
import uk.ac.ox.oxfish.model.data.collectors.DataColumn;
import uk.ac.ox.oxfish.model.market.itq.ITQOrderBook;
import uk.ac.ox.oxfish.model.market.itq.MonoQuotaPriceGenerator;
import uk.ac.ox.oxfish.model.market.itq.PriceGenerator;
import uk.ac.ox.oxfish.model.market.itq.PricingPolicy;
import uk.ac.ox.oxfish.model.regs.MultipleRegulations;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.util.HashMap;
import java.util.HashSet;
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

    private final HashSet<Regulation> traders = new HashSet<>();

    public void addTrader(Regulation regulation)
    {
        traders.add(regulation);
    }


    public ITQMarketBuilder(
            int speciesIndex,
            Supplier<PriceGenerator> priceGeneratorMaker) {
        this.speciesIndex = speciesIndex;
        this.priceGeneratorMaker = priceGeneratorMaker;
        market = new ITQOrderBook(speciesIndex, 1,
                                  new PricingPolicy() {
                                      @Override
                                      public double tradePrice(
                                              double askPrice, double bidPrice, double secondBestAsk,
                                              double secondBestBid) {
                                          return askPrice;
                                      }
                                  }, 7);

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
        if(model.getDailyDataSet().getColumn("ITQ Trades Of " + speciesName)==null) {

            //gather market data
            model.getDailyDataSet().registerGatherer("ITQ Trades Of " + speciesName, new Gatherer<FishState>() {
                                                         @Override
                                                         public Double apply(FishState state1) {
                                                             return market.getDailyMatches();
                                                         }
                                                     },
                                                     Double.NaN);
            DataColumn priceGatherer =
                    model.getDailyDataSet().registerGatherer("ITQ Prices Of " + speciesName,
                                                             new Gatherer<FishState>() {
                                                                 @Override
                                                                 public Double apply(FishState state1) {
                                                                     return market.getDailyAveragePrice();
                                                                 }

                                                             },
                                                             Double.NaN);
            model.getYearlyDataSet().registerGatherer("ITQ Prices Of " + speciesName,
                                                      FishStateUtilities.generateYearlyAverage(priceGatherer),
                                                      Double.NaN);


            model.getDailyDataSet().registerGatherer("ITQ Last Closing Price Of " + speciesName, state1 -> {
                                                         return market.getLastClosingPrice();
                                                     },
                                                     Double.NaN);
            DataColumn volumeGatherer = model.getDailyDataSet().registerGatherer("ITQ Volume Of " + speciesName,
                                                                                 state1 -> {
                                                                                     return market.getDailyQuotasExchanged();
                                                                                 },
                                                                                 Double.NaN);
            model.getYearlyDataSet().registerGatherer("ITQ Volume Of " + speciesName,
                                                      FishStateUtilities.generateYearlySum(volumeGatherer),
                                                      0d);


            model.getDailyDataSet().registerGatherer("ITQ Trade Value Of " + speciesName, state1 -> {
                                                         return market.getDailyQuotasExchanged() * market.getDailyAveragePrice();
                                                     },
                                                     Double.NaN);

        }

        //make it annual too


        //and give to each fisher a price-maker
        for(Fisher fisher : model.getFishers()) {
            //todo remove this ugly hack~!
           if (traders.contains(fisher.getRegulation()) ||
                   (fisher.getRegulation() instanceof MultipleRegulations && isMatch(
                           ((MultipleRegulations) fisher.getRegulation())))) {
                PriceGenerator reservationPricer = priceGeneratorMaker.get();
                reservationPricer.start(model, fisher);
                market.registerTrader(fisher, reservationPricer);
                //record it
                reservationPricers.put(fisher, reservationPricer);
            }

        }


    }

    /**
     * checks if the multiple regulation is valid for this market
     * @param regulations
     * @return
     */
    private boolean isMatch(MultipleRegulations regulations){

        for(Regulation trader : traders)
            if(regulations.containsRegulation(trader))
                return true;
        return false;


    }


    /**
     * tell the startable to turnoff,
     */
    @Override
    public void turnOff() {
        market.turnOff();
        for(PriceGenerator pricer : reservationPricers.values())
            pricer.turnOff(pricer.getFisher());
    }


    public PriceGenerator getReservationPriceGenerator(Fisher fisher)
    {
        return reservationPricers.get(fisher);
    }

    public ITQOrderBook getMarket() {
        return market;
    }
}
