/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2020  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.biology.growers;

import org.junit.Test;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.factory.NoFishingFactory;
import uk.ac.ox.oxfish.model.scenario.TunaScenario;

import java.nio.file.Paths;

import static org.junit.Assert.*;

public class FadAwareCommonLogisticGrowerTest {


    @Test
    public void jonLandings() {
        TunaScenario.EXOGENOUS_CATCHES_FILE = Paths.get("inputs","tests","exogenous_catches.csv");

        TunaScenario scenario = new TunaScenario();
        scenario.getFisherDefinition().setRegulation(new NoFishingFactory());

        FishState state = new FishState();
        state.setScenario(scenario);

        state.start();

        while(state.getYear()<5) {
            state.schedule.step(state);
            System.out.println(
                    state.getTotalBiomass(state.getBiology().getSpecie("Yellowfin tuna"))
            );
        }
        state.schedule.step(state);

        System.out.println(
                state.getTotalBiomass(state.getBiology().getSpecie("Yellowfin tuna"))
        );

        assertTrue( Math.abs(889195.40-state.getTotalBiomass(state.getBiology().getSpecie("Yellowfin tuna"))/1000d)<10);



    }
}