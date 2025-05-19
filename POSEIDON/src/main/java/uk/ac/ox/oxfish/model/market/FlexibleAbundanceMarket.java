/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

package uk.ac.ox.oxfish.model.market;

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Hold;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.Gatherer;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

/**
 * The most "generic" market for "abundance" species.
 * The pricing behaviour is commanded by a pricing strategy which should be fully swappable
 */
public class FlexibleAbundanceMarket extends AbstractMarket {


    public static final String AGE_BIN_PREFIX = " - age bin ";
    /**
     * the object deciding the prices
     */
    private PricingStrategy pricingStrategy;


    public FlexibleAbundanceMarket(PricingStrategy pricingStrategy) {

        this.pricingStrategy = pricingStrategy;

    }

    @Override
    protected TradeInfo sellFishImplementation(
        Hold hold, Fisher fisher,
        Regulation regulation,
        FishState state, Species species
    ) {


        //find out legal biomass sold
        double proportionActuallySellable =
            Math.min(
                1d,
                regulation.maximumBiomassSellable(
                    fisher,
                    species,
                    state
                ) /
                    hold.getWeightOfCatchInHold(species)
            );
        assert proportionActuallySellable >= 0;
        assert proportionActuallySellable <= 1;

        if (proportionActuallySellable == 0)
            return new TradeInfo(0, species, 0d);


        double earnings = 0;
        double sold = 0;
        for (int age = 0; age < species.getNumberOfBins(); age++) {

            double soldThisBin = hold.getWeightOfBin(species, age);
            //reweight because you might be not allowed to sell more than x
            soldThisBin *= proportionActuallySellable;
            //if there is nothing to sell here...
            if (soldThisBin <= 0) {
                //sometimes this becomes -1e-35
                assert soldThisBin >= -FishStateUtilities.EPSILON;
                continue;
            }

            //look for the correct price bin
            double priceForThisBin = pricingStrategy.getPricePerKg(
                species,
                fisher,
                age,
                soldThisBin
            );

            getDailyCounter().count(LANDINGS_COLUMN_NAME + " - age bin " + age, soldThisBin);
            earnings += soldThisBin * priceForThisBin;
            getDailyCounter().count(EARNINGS_COLUMN_NAME + " - age bin " + age, soldThisBin * priceForThisBin);

            sold += soldThisBin;
        }

        //give fisher the money
        fisher.earn(earnings);

        //tell regulation
        regulation.reactToSale(species, fisher, sold, earnings, state);

        //return data!
        return new TradeInfo(sold, species, earnings);
    }

    @Override
    public double getMarginalPrice() {
        return pricingStrategy.getMarginalPrice();
    }

    @Override
    public void start(FishState state) {
        super.start(state);

        for (int age = 0; age < getSpecies().getNumberOfBins(); age++) {
            String columnName = LANDINGS_COLUMN_NAME + AGE_BIN_PREFIX + age;
            getDailyCounter().addColumn(columnName);
            String finalColumnName1 = columnName;
            getData().registerGatherer(columnName,
                (Gatherer<Market>) market -> getDailyCounter().getColumn(finalColumnName1),
                0
            );


            columnName = EARNINGS_COLUMN_NAME + " - age bin " + age;
            getDailyCounter().addColumn(columnName);
            String finalColumnName = columnName;
            getData().registerGatherer(columnName,
                (Gatherer<Market>) market -> getDailyCounter().getColumn(finalColumnName),
                0
            );
        }
    }


    public PricingStrategy getPricingStrategy() {
        return pricingStrategy;
    }

    public void setPricingStrategy(PricingStrategy pricingStrategy) {
        this.pricingStrategy = pricingStrategy;
    }
}

