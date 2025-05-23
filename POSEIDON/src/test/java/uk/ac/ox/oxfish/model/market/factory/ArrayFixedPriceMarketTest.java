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

package uk.ac.ox.oxfish.model.market.factory;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.biology.initializer.factory.SplitInitializerFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;

/**
 * Created by carrknight on 8/31/16.
 */
public class ArrayFixedPriceMarketTest {


    @Test
    public void fixedPrice() throws Exception {


        PrototypeScenario scenario = new PrototypeScenario();
        scenario.setFishers(0);
        scenario.setBiologyInitializer(new SplitInitializerFactory());
        ArrayFixedPriceMarket market = new ArrayFixedPriceMarket();
        market.setPrices("20,-10");
        scenario.setMarket(market);
        FishState state = new FishState();
        state.setScenario(scenario);

        state.start();
        state.schedule.step(state);

        double firstPrice = state.getPorts().iterator().next().getDefaultMarketMap().getMarket(
            state.getSpecies().get(0)).getMarginalPrice();
        double secondPrice = state.getPorts().iterator().next().getDefaultMarketMap().getMarket(
            state.getSpecies().get(1)).getMarginalPrice();
        Assertions.assertEquals(firstPrice, 20, .0001);
        Assertions.assertEquals(secondPrice, -10, .0001);


    }
}
