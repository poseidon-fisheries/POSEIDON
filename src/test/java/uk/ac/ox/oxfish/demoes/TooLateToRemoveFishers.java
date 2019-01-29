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

import com.esotericsoftware.minlog.Log;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.growers.SimpleLogisticGrowerFactory;
import uk.ac.ox.oxfish.biology.initializer.factory.DiffusingLogisticFactory;
import uk.ac.ox.oxfish.geography.mapmakers.SimpleMapInitializerFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import static org.junit.Assert.assertTrue;

/**
 * Created by carrknight on 12/15/15.
 */
public class TooLateToRemoveFishers
{


    @Test
    public void tooLateToRemoveFishers() throws Exception {
        FishState state = new FishState(System.currentTimeMillis());

        Log.info("This demo replicates the dynamics in: http://carrknight.github.io/assets/oxfish/entryexit.html");
        Log.info("You add a bunch of fishers, and after removing them the biomass is still screwed");
        PrototypeScenario scenario = new PrototypeScenario();
        ((DiffusingLogisticFactory) scenario.getBiologyInitializer()).setGrower(new SimpleLogisticGrowerFactory(.3));
        scenario.setFishers(50);
        SimpleMapInitializerFactory simpleMapInitializerFactory = new SimpleMapInitializerFactory();
        simpleMapInitializerFactory.setCoastalRoughness(new FixedDoubleParameter(0));
        scenario.setMapInitializer(simpleMapInitializerFactory);

        //run the model for a full 3 years before progressing
        state.setScenario(scenario);
        state.start();
        while (state.getYear() < 3)
            state.schedule.step(state);

        //now keep running for 15 years adding 3 fishers every month
        while (state.getYear() < 18) {
            if (state.getDayOfTheYear() % 30 == 0) {
                state.createFisher(FishState.DEFAULT_POPULATION_NAME);
                state.createFisher(FishState.DEFAULT_POPULATION_NAME);
                state.createFisher(FishState.DEFAULT_POPULATION_NAME);
                //   state.createFisher();
                //   state.createFisher();
            }
            state.schedule.step(state);
        }
        Double biomass = state.getLatestYearlyObservation("Biomass Species 0");
        Log.info("The actual remaining biomass is: " + biomass);
        assertTrue(biomass < 1000000);

        //for the next 5 years remove the fishers
        while (state.getYear() < 23) {
            if (state.getDayOfTheYear() % 30 == 0) {
                state.killRandomFisher();
                state.killRandomFisher();
                state.killRandomFisher();
                //    state.killRandomFisher();
                //    state.killRandomFisher();
            }
            state.schedule.step(state);
        }

        Log.info("I am assuming that the biomass is below 10% the virgin level of 10million");
        biomass = state.getLatestYearlyObservation("Biomass Species 0");
        Log.info("The actual remaining biomass is: " + biomass);
        assertTrue(biomass < 1000000);
    }

}
