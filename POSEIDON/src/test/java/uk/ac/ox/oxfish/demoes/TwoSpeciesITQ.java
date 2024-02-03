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

package uk.ac.ox.oxfish.demoes;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.biology.initializer.factory.WellMixedBiologyFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.market.AbstractMarket;
import uk.ac.ox.oxfish.model.market.FixedPriceMarket;
import uk.ac.ox.oxfish.model.market.Market;
import uk.ac.ox.oxfish.model.regs.factory.MultiITQFactory;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

import java.util.Iterator;
import java.util.List;

public class TwoSpeciesITQ {


    @Test
    public void bluesAreWorthlessButQuotasSoRareTheyEndUpCostingMore() throws Exception {


        final FishState state = new FishState(System.currentTimeMillis());

        MultiITQFactory multiFactory = new MultiITQFactory();
        //quota ratios: 90-10
        multiFactory.setQuotaFirstSpecie(new FixedDoubleParameter(4500));
        multiFactory.setQuotaOtherSpecies(new FixedDoubleParameter(500));
        //wellmixed biomass ratio: 70-30
        WellMixedBiologyFactory biologyFactory = new WellMixedBiologyFactory();
        biologyFactory.setCapacityRatioSecondToFirst(new FixedDoubleParameter(.3));


        PrototypeScenario scenario = new PrototypeScenario();
        state.setScenario(scenario);
        //world split in half
        scenario.setBiologyInitializer(biologyFactory);
        scenario.setRegulation(multiFactory);


        //sale price is 10
        scenario.setMarket(state1 -> new FixedPriceMarket(10));


        scenario.setUsePredictors(true);

        //make species 2 worthless
        state.registerStartable(new Startable() {
            @Override
            public void start(FishState model) {
                List<Market> markets = state.getAllMarketsForThisSpecie(state.getSpecies().get(1));
                assert markets.size() == 1;
                ((FixedPriceMarket) markets.get(0)).setPrice(0d);


            }

            @Override
            public void turnOff() {

            }
        });


        state.start();
        while (state.getYear() < 10)
            state.schedule.step(state);


        //reds don't use a lot of biomass: less than 50% of allocated red quota is landed
        Double redLandings = state.getYearlyDataSet().getColumn(
            state.getSpecies().get(0) + " " + AbstractMarket.LANDINGS_COLUMN_NAME).getLatest();
        Assertions.assertTrue(4500 * scenario.getFishers() * .5 > redLandings);
        //at least 95% of the blue quota was consumed instead
        Double blueLandings = state.getYearlyDataSet().getColumn(
            state.getSpecies().get(1) + " " + AbstractMarket.LANDINGS_COLUMN_NAME).getLatest();
        Assertions.assertTrue(500 * scenario.getFishers() * .95 < blueLandings);
        System.out.println(redLandings + " ---- "
            + blueLandings);
        System.out.println(redLandings / (4500 * scenario.getFishers()) + " ---- "
            + blueLandings / (500 * scenario.getFishers()));


        //red quotas are cheap
        double highestRed = 0;
        Iterator<Double> redIterator = state.getDailyDataSet().getColumn(
            "ITQ Prices Of Species " + 0).descendingIterator();
        for (int i = 0; i < 365; i++) {
            double current = redIterator.next();
            if (Double.isFinite(current) && current > highestRed)
                highestRed = current;
        }
        Assertions.assertTrue(highestRed >= 0);
        Assertions.assertTrue(highestRed < 7, highestRed + " ----- ");

        //blue quotas are pricey!
        double highestBlue = 0;
        Iterator<Double> blueIterator = state.getDailyDataSet().getColumn(
            "ITQ Prices Of Species " + 1).descendingIterator();
        for (int i = 0; i < 365; i++) {
            double current = blueIterator.next();
            if (Double.isFinite(current) && current > highestBlue)
                highestBlue = current;
        }
        System.out.println(highestRed + " ----- " + highestBlue);

        //more than the sale price of red!
        Assertions.assertTrue(highestBlue > 10, highestRed + " ----- " + highestBlue);

        System.out.println("============================================");
    }


}
