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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.biology.initializer.factory.HalfBycatchFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.itq.ITQOrderBook;
import uk.ac.ox.oxfish.model.regs.MultiQuotaITQRegulation;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;

/**
 * Created by carrknight on 5/13/17.
 */
public class MultiITQStringFactoryTest {


    @Test
    public void createRightQuotaMarkets() throws Exception {

        FishState state = new FishState();
        PrototypeScenario scenario = new PrototypeScenario();
        scenario.setBiologyInitializer(new HalfBycatchFactory());
        scenario.setFishers(1);
        MultiITQStringFactory factory = new MultiITQStringFactory();
        factory.setYearlyQuotaMaps("0:100,1:200");
        factory.setMinimumQuotaTraded(" 99");
        scenario.setRegulation(factory);
        state.setScenario(scenario);
        state.start();
        state.schedule.step(state);
        MultiQuotaITQRegulation regulation = (MultiQuotaITQRegulation) state.getFishers().get(0).getRegulation();
        ITQOrderBook itqOrderBook = regulation.testOrderBook(state.getSpecies().get(0));
        Assertions.assertFalse(itqOrderBook.isAllowMultipleTradesPerFisher());
        Assertions.assertEquals(itqOrderBook.getUnitsTradedPerMatch(), 99);
        Assertions.assertEquals(regulation.getQuotaRemaining(0), 100d, .0001);


        itqOrderBook = regulation.testOrderBook(state.getSpecies().get(1));
        Assertions.assertFalse(itqOrderBook.isAllowMultipleTradesPerFisher());
        Assertions.assertEquals(itqOrderBook.getUnitsTradedPerMatch(), 99);
        Assertions.assertEquals(regulation.getQuotaRemaining(1), 200d, .0001);

    }

    @Test
    public void heterogeneousVolume() throws Exception {

        FishState state = new FishState();
        PrototypeScenario scenario = new PrototypeScenario();
        scenario.setBiologyInitializer(new HalfBycatchFactory());
        scenario.setFishers(1);
        MultiITQStringFactory factory = new MultiITQStringFactory();
        factory.setYearlyQuotaMaps("0:100,1:200");
        factory.setMinimumQuotaTraded(" 0:99,1:2");
        scenario.setRegulation(factory);
        state.setScenario(scenario);
        state.start();
        state.schedule.step(state);
        MultiQuotaITQRegulation regulation = (MultiQuotaITQRegulation) state.getFishers().get(0).getRegulation();
        ITQOrderBook itqOrderBook = regulation.testOrderBook(state.getSpecies().get(0));
        Assertions.assertFalse(itqOrderBook.isAllowMultipleTradesPerFisher());
        Assertions.assertEquals(itqOrderBook.getUnitsTradedPerMatch(), 99);
        Assertions.assertEquals(regulation.getQuotaRemaining(0), 100d, .0001);


        itqOrderBook = regulation.testOrderBook(state.getSpecies().get(1));
        Assertions.assertFalse(itqOrderBook.isAllowMultipleTradesPerFisher());
        Assertions.assertEquals(itqOrderBook.getUnitsTradedPerMatch(), 2);
        Assertions.assertEquals(regulation.getQuotaRemaining(1), 200d, .0001);

    }
}
