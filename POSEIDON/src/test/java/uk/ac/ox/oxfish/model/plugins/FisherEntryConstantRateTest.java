/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2019  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.model.plugins;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.FishingSeason;
import uk.ac.ox.oxfish.model.scenario.FlexibleScenario;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

public class FisherEntryConstantRateTest {


    @Test
    public void acceptanceTest() {

        //50% increase given 10 fishers
        FlexibleScenario scenario = new FlexibleScenario();
        scenario.getFisherDefinitions().get(0).getInitialFishersPerPort().clear();
        scenario.getFisherDefinitions().get(0).getInitialFishersPerPort().put("Port 0", 10);

        FisherEntryConstantRateFactory entryRate = new FisherEntryConstantRateFactory();
        entryRate.setGrowthRateInPercentage(new FixedDoubleParameter(.5));
        entryRate.setPopulationName("population0");
        scenario.getPlugins().add(entryRate);

        FishState state = new FishState();
        state.setScenario(scenario);

        state.start();
        while (state.getYear() < 2)
            state.schedule.step(state);

        //year 0: 10
        //year 1: 15
        //year 2: 22.5 rounded to 23
        Assertions.assertEquals(state.getFishers().size(), 23);

        //now force all but 10 people to stay home; the active fishers left are 10 which means that the increase is going to be 5 fishers
        for (int i = 10; i < state.getFishers().size(); i++) {
            state.getFishers().get(i).setRegulation(new FishingSeason(true, 0));

        }
        while (state.getYear() < 3)
            state.schedule.step(state);
        Assertions.assertEquals(state.getFishers().size(), 28);

    }
}
