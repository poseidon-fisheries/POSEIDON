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

package uk.ac.ox.oxfish.fisher.strategies.departing.factory;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.strategies.departing.GiveUpAfterSomeLossesThisYearDecorator;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.factory.FixedPriceMarketFactory;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

public class GiveUpAfterSomeLossesThisYearFactoryTest {


    @Test
    public void control150Days() {

        //create a scenario where you always lose money
        //people won't give up within a year with the usual setup
        PrototypeScenario scenario = new PrototypeScenario();
        scenario.setFishers(20);
        final FixedPriceMarketFactory market = new FixedPriceMarketFactory();
        market.setMarketPrice(new FixedDoubleParameter(-100));
        scenario.setGasPricePerLiter(new FixedDoubleParameter(1));
        scenario.setMarket(market);

        FishState state = new FishState();
        state.setScenario(scenario);
        state.start();

        for (int i = 0; i < 150; i++)
            state.schedule.step(state);

        Assertions.assertTrue(state.getLatestDailyObservation("Fishers at Sea") > 0);


    }

    @Test
    public void treatment150Days() {

        //create a scenario where you always lose money
        //people won't give up within a year with the usual setup
        PrototypeScenario scenario = new PrototypeScenario();
        final GiveUpAfterSomeLossesThisYearFactory departingStrategy = new GiveUpAfterSomeLossesThisYearFactory();
        departingStrategy.setDelegate(new MaxHoursPerYearDepartingFactory(9999999));
        scenario.setDepartingStrategy(
            departingStrategy
        );
        scenario.setFishers(20);
        final FixedPriceMarketFactory market = new FixedPriceMarketFactory();
        market.setMarketPrice(new FixedDoubleParameter(-100));
        scenario.setGasPricePerLiter(new FixedDoubleParameter(1));
        scenario.setMarket(market);

        FishState state = new FishState();
        state.setScenario(scenario);
        state.start();

        for (int i = 0; i < 150; i++)
            state.schedule.step(state);

        Assertions.assertEquals(state.getLatestDailyObservation("Fishers at Sea"), 0, .0001);
        //rests at the beginning of the year
        while (state.getYear() < 1)
            state.schedule.step(state);
        state.schedule.step(state);
        Assertions.assertTrue(state.getLatestDailyObservation("Fishers at Sea") > 0);


    }

    @Test
    public void disableTest() {

        //create a scenario where you always lose money
        //people won't give up within a year with the usual setup
        PrototypeScenario scenario = new PrototypeScenario();
        final GiveUpAfterSomeLossesThisYearFactory departingStrategy = new GiveUpAfterSomeLossesThisYearFactory();
        departingStrategy.setDelegate(new MaxHoursPerYearDepartingFactory(9999999));
        scenario.setDepartingStrategy(
            departingStrategy
        );

        scenario.setFishers(20);
        final FixedPriceMarketFactory market = new FixedPriceMarketFactory();
        market.setMarketPrice(new FixedDoubleParameter(-100));
        scenario.setGasPricePerLiter(new FixedDoubleParameter(1));
        scenario.setMarket(market);

        FishState state = new FishState();
        state.setScenario(scenario);
        state.start();

        //they should quit, but I disable it
        for (Fisher fisher : state.getFishers()) {
            ((GiveUpAfterSomeLossesThisYearDecorator) fisher.getDepartingStrategy()).disable();
        }
        for (int i = 0; i < 150; i++) {
            state.schedule.step(state);
            System.out.println(state.getLatestDailyObservation("Fishers at Sea"));
        }
        // assertEquals(state.getLatestDailyObservation("Fishers at Sea"), 0,.0001);

        Assertions.assertTrue(state.getLatestDailyObservation("Fishers at Sea") > 0);


    }
}
