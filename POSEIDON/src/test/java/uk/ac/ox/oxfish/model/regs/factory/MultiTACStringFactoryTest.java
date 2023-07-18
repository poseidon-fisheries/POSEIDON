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

import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.biology.initializer.factory.HalfBycatchFactory;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.MultiQuotaRegulation;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class MultiTACStringFactoryTest {


    @Test
    public void createCorrectRule() throws Exception {


        PrototypeScenario scenario = new PrototypeScenario();
        MultiTACStringFactory regulation = new MultiTACStringFactory();
        scenario.setRegulation(regulation);
        scenario.setBiologyInitializer(new HalfBycatchFactory());

        regulation.setYearlyQuotaMaps("1:123");

        FishState state = new FishState(System.currentTimeMillis());
        state.setScenario(scenario);

        state.start();
        Fisher fisher = state.getFishers().get(0);
        MultiQuotaRegulation regs = (MultiQuotaRegulation) fisher.getRegulation();
        assertTrue(Double.isInfinite(regs.getQuotaRemaining(0)));
        assertEquals(regs.getQuotaRemaining(1), 123d, .0001);


    }

    @Test
    public void emptyRule() throws Exception {


        PrototypeScenario scenario = new PrototypeScenario();
        MultiTACStringFactory regulation = new MultiTACStringFactory();
        scenario.setRegulation(regulation);
        scenario.setBiologyInitializer(new HalfBycatchFactory());

        regulation.setYearlyQuotaMaps("0:1.0,1:1.0");

        FishState state = new FishState(System.currentTimeMillis());
        state.setScenario(scenario);

        state.start();
        Fisher fisher = state.getFishers().get(0);
        MultiQuotaRegulation regs = (MultiQuotaRegulation) fisher.getRegulation();
        assertEquals(regs.getQuotaRemaining(0), 1d, .0001);
        assertEquals(regs.getQuotaRemaining(1), 1d, .0001);


    }
}