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

package uk.ac.ox.oxfish.model.scenario;

import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Anarchy;
import uk.ac.ox.oxfish.model.regs.factory.AnarchyFactory;
import uk.ac.ox.oxfish.model.regs.factory.TACMonoFactory;

import static org.junit.Assert.*;

/**
 * Created by carrknight on 4/3/17.
 */
public class TwoPopulationsScenarioTest {


    @Test
    public void nonseparateRegs() throws Exception {

        TwoPopulationsScenario scenario = new TwoPopulationsScenario();
        //quick
        scenario.setLargeFishers(1);
        scenario.setSmallFishers(1);
        scenario.setRegulationSmall(new AnarchyFactory());
        scenario.setRegulationLarge(new TACMonoFactory());

        scenario.setSeparateRegulations(false); //force everybody to use small boats regulations
        FishState state = new FishState();
        state.setScenario(scenario);
        state.start();
        //they are all anarchy!
        for(Fisher fisher : state.getFishers())
            assertTrue(fisher.getRegulation() instanceof Anarchy);

    }


    @Test
    public void separateRegs() throws Exception {

        TwoPopulationsScenario scenario = new TwoPopulationsScenario();
        //quick
        scenario.setLargeFishers(1);
        scenario.setSmallFishers(1);
        scenario.setRegulationSmall(new AnarchyFactory());
        scenario.setRegulationLarge(new TACMonoFactory());

        scenario.setSeparateRegulations(true); //force everybody to use small boats regulations
        FishState state = new FishState();
        state.setScenario(scenario);
        state.start();
        //they are all anarchy!
        int nonAnarchy = 0;
        for(Fisher fisher : state.getFishers()) {
            if (fisher.getTags().contains("small"))
                assertTrue(fisher.getRegulation() instanceof Anarchy);
            else {
                nonAnarchy++;
                assertFalse(fisher.getRegulation() instanceof Anarchy);
            }
        }
        //make sure you are counting them!
        assertEquals(nonAnarchy,1);

    }
}