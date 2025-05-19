/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
import java.util.Iterator;
import java.util.function.Supplier;

/**
 * A startable useful to create an ITQ market and to create the ITQ reservation pricer.
 * Created by carrknight on 9/22/15.
 */
public class ITQMarketBuilder implements Startable {


    private final int speciesIndex;
    final private Supplier<PriceGenerator> priceGeneratorMaker;
    private final ITQOrderBook market;
    private final HashSet<Regulation> traders = new HashSet<>();
    /**
     * the generators of reservation prices for each fisher
     */
    private HashMap<Fisher, PriceGenerator> reservationPricers = new HashMap<>();

    /**
     * creates an ITQ market using MonoQuotaPriceGenerator
     *
     * @param speciesIndex
     */
    public ITQMarketBuilder(int speciesIndex) {

        this(speciesIndex, () -> new MonoQuotaPriceGenerator(speciesIndex, false));
    }


    public ITQMarketBuilder(
        int speciesIndex,
        Supplier<PriceGenerator> priceGeneratorMaker
    ) {
        this.speciesIndex = speciesIndex;
        this.priceGeneratorMaker = priceGeneratorMaker;
        market = new ITQOrderBook(speciesIndex, 1,
            (PricingPolicy) (askPrice, bidPrice, secondBestAsk, secondBestBid) -> askPrice, 7
        );

    }

    public void addTrader(Regulation regulation) {
        traders.add(regulation);
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
        if (model.getDailyDataSet().getColumn("ITQ Trades Of " + speciesName) == null) {

            //gather market data
            model.getDailyDataSet().registerGatherer("ITQ Trades Of " + speciesName,
                (Gatherer<FishState>) state1 -> market.getDailyMatches(),
                Double.NaN
            );
            DataColumn priceGatherer =
                model.getDailyDataSet().registerGatherer(
                    "ITQ Prices Of " + speciesName,
                    (Gatherer<FishState>) state1 -> market.getDailyAveragePrice(),
                    Double.NaN
                );
            model.getYearlyDataSet().registerGatherer(
                "ITQ Prices Of " + speciesName,
                FishStateUtilities.generateYearlyAverage(priceGatherer),
                Double.NaN
            );


            model.getDailyDataSet().registerGatherer("ITQ Last Closing Price Of " + speciesName, state1 -> {
                    return market.getLastClosingPrice();
                },
                Double.NaN
            );
            DataColumn volumeGatherer = model.getDailyDataSet().registerGatherer(
                "ITQ Volume Of " + speciesName,
                state1 -> {
                    return market.getDailyQuotasExchanged();
                },
                Double.NaN
            );
            model.getYearlyDataSet().registerGatherer(
                "ITQ Volume Of " + speciesName,
                FishStateUtilities.generateYearlySum(volumeGatherer),
                0d
            );


            DataColumn tradeValueColumn = model.getDailyDataSet().registerGatherer(
                "ITQ Trade Value Of " + speciesName,
                state1 -> {
                    return market.getDailyQuotasExchanged() * market.getDailyAveragePrice();
                },
                Double.NaN
            );

            //these is probably more correct as a measure of what the prices of stuff traded were!
            model.getYearlyDataSet().registerGatherer(
                "ITQ Weighted Prices Of " + speciesName,
                (Gatherer<FishState>) fishState -> {

                    final Iterator<Double> numeratorIterator = tradeValueColumn.descendingIterator();
                    final Iterator<Double> denominatorIterator = volumeGatherer.descendingIterator();
                    if (!numeratorIterator.hasNext()) //not ready/year 1
                        return Double.NaN;
                    double numerator = 0;
                    double denominator = 0;
                    for (int i = 0; i < 365; i++) {
                        //it should be step 365 times at most, but it's possible that this agent was added halfway through
                        //and only has a partially filled collection
                        if (numeratorIterator.hasNext()) {
                            assert denominatorIterator.hasNext();
                            Double tradeValue = numeratorIterator.next();
                            if (Double.isFinite(tradeValue))
                                numerator += tradeValue;
                            denominator += denominatorIterator.next();
                        }
                    }

                    return numerator / denominator;


                },
                Double.NaN
            );

        }

        //make it annual too


        //and give to each fisher a price-maker
        for (Fisher fisher : model.getFishers()) {
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
     *
     * @param regulations
     * @return
     */
    private boolean isMatch(MultipleRegulations regulations) {

        for (Regulation trader : traders)
            if (regulations.containsRegulation(trader))
                return true;
        return false;


    }


    /**
     * tell the startable to turnoff,
     */
    @Override
    public void turnOff() {
        market.turnOff();
        for (PriceGenerator pricer : reservationPricers.values())
            pricer.turnOff(pricer.getFisher());
    }


    public PriceGenerator getReservationPriceGenerator(Fisher fisher) {
        return reservationPricers.get(fisher);
    }

    public ITQOrderBook getMarket() {
        return market;
    }
}
