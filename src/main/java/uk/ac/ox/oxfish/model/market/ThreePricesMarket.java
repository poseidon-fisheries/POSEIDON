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

package uk.ac.ox.oxfish.model.market;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Hold;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.Gatherer;
import uk.ac.ox.oxfish.model.regs.Regulation;

/**
 *
 * There are 2 thresholds and three prices (one below the first threshold, one in between and one above)
 * where the thresholds are in terms of "age"
 *
 * Created by carrknight on 7/4/17.
 */
public class ThreePricesMarket extends AbstractMarket {


    public static final String AGE_BIN_PREFIX = " - age bin ";
    private int lowAgeThreshold;

    private int highAgeThreshold;

    private double priceBelowThreshold;

    private double priceBetweenThresholds;

    private double priceAboveThresholds;



    public ThreePricesMarket(
            int lowAgeThreshold,
            int highAgeThreshold,
            double priceBelowThreshold,
            double priceBetweenThresholds,
            double priceAboveThresholds) {
        this.lowAgeThreshold = lowAgeThreshold;
        this.highAgeThreshold = highAgeThreshold;
        Preconditions.checkArgument(highAgeThreshold>lowAgeThreshold);
        this.priceBelowThreshold = priceBelowThreshold;
        this.priceBetweenThresholds = priceBetweenThresholds;
        this.priceAboveThresholds = priceAboveThresholds;
    }


    /**
     * starts gathering data. If called multiple times all the calls after the first are ignored
     *
     * @param state the model
     */
    @Override
    public void start(FishState state) {
        super.start(state);


        for(int age =0; age<getSpecies().getNumberOfBins();age++) {
            String columnName = LANDINGS_COLUMN_NAME + AGE_BIN_PREFIX + age;
            getDailyCounter().addColumn(columnName);
            String finalColumnName1 = columnName;
            getData().registerGatherer(columnName, new Gatherer<Market>() {
                                           @Override
                                           public Double apply(Market market) {
                                               return getDailyCounter().getColumn(finalColumnName1);
                                           }
                                       },
                                       0);


            columnName = EARNINGS_COLUMN_NAME + " - age bin " + age;
            getDailyCounter().addColumn(columnName);
            String finalColumnName = columnName;
            getData().registerGatherer(columnName, new Gatherer<Market>() {
                                           @Override
                                           public Double apply(Market market) {
                                               return getDailyCounter().getColumn(finalColumnName);
                                           }
                                       },
                                       0);
        }
    }

    /**
     * returns the average marginal price
     *
     * @return price
     */
    @Override
    public double getMarginalPrice() {
        return (priceBelowThreshold +
                priceBetweenThresholds +
                priceAboveThresholds)/3d;
    }

    @Override
    protected TradeInfo sellFishImplementation(
            Hold hold,
            Fisher fisher,
            Regulation regulation,
            FishState state,
            Species species)
    {



        //find out legal biomass sold
        double proportionActuallySellable =
                Math.min(1d,
                         regulation.maximumBiomassSellable(fisher,
                                                           species,
                                                           state)/
                                 hold.getWeightOfCatchInHold(species));
        assert proportionActuallySellable>=0;
        assert proportionActuallySellable<=1;

        if(proportionActuallySellable == 0)
            return new TradeInfo(0,species,0d);

        //for each age find price sold at and quantity sold
        double price =priceBelowThreshold;
        double earnings = 0;
        double sold = 0;
        for(int age =0; age<species.getNumberOfBins();age++)
        {
            if(age>highAgeThreshold)
                price= priceAboveThresholds;
            else
            if(age>lowAgeThreshold)
                price=priceBetweenThresholds;

            double soldThisBin =  hold.getWeightOfBin(species,age);
            //reweight because you might be not allowed to sell more than x
            soldThisBin *= proportionActuallySellable;
            getDailyCounter().count(LANDINGS_COLUMN_NAME + " - age bin " + age,soldThisBin);
            earnings+= soldThisBin *price;
            getDailyCounter().count(EARNINGS_COLUMN_NAME + " - age bin " + age,soldThisBin *price);

            sold+= soldThisBin;
        }

        //give fisher the money
        fisher.earn(earnings);

        //tell regulation
        regulation.reactToSale(species, fisher , sold, earnings);

        //return data!
        return new TradeInfo(sold,species,earnings);
    }

    public int getLowAgeThreshold() {
        return lowAgeThreshold;
    }

    public void setLowAgeThreshold(int lowAgeThreshold) {
        this.lowAgeThreshold = lowAgeThreshold;
    }

    public int getHighAgeThreshold() {
        return highAgeThreshold;
    }

    public void setHighAgeThreshold(int highAgeThreshold) {
        this.highAgeThreshold = highAgeThreshold;
    }

    public double getPriceBelowThreshold() {
        return priceBelowThreshold;
    }

    public void setPriceBelowThreshold(double priceBelowThreshold) {
        this.priceBelowThreshold = priceBelowThreshold;
    }

    public double getPriceBetweenThresholds() {
        return priceBetweenThresholds;
    }

    public void setPriceBetweenThresholds(double priceBetweenThresholds) {
        this.priceBetweenThresholds = priceBetweenThresholds;
    }

    public double getPriceAboveThresholds() {
        return priceAboveThresholds;
    }

    public void setPriceAboveThresholds(double priceAboveThresholds) {
        this.priceAboveThresholds = priceAboveThresholds;
    }
}
