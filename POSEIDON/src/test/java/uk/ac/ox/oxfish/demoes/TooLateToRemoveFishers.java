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

package uk.ac.ox.oxfish.demoes;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.biology.growers.SimpleLogisticGrowerFactory;
import uk.ac.ox.oxfish.biology.initializer.factory.DiffusingLogisticFactory;
import uk.ac.ox.oxfish.geography.mapmakers.SimpleMapInitializerFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

import java.util.logging.Logger;

/**
 * Created by carrknight on 12/15/15.
 */
public class TooLateToRemoveFishers {


    @Test
    public void tooLateToRemoveFishers() throws Exception {
        final FishState state = new FishState(System.currentTimeMillis());

        Logger.getGlobal()
            .info("This demo replicates the dynamics in: http://carrknight.github.io/assets/oxfish/entryexit.html");
        Logger.getGlobal().info("You add a bunch of fishers, and after removing them the biomass is still screwed");
        final PrototypeScenario scenario = new PrototypeScenario();
        ((DiffusingLogisticFactory) scenario.getBiologyInitializer()).setGrower(new SimpleLogisticGrowerFactory(.3));
        scenario.setFishers(50);
        final SimpleMapInitializerFactory simpleMapInitializerFactory = new SimpleMapInitializerFactory();
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
        Logger.getGlobal().info("The actual remaining biomass is: " + biomass);
        Assertions.assertTrue(biomass < 1000000);

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

        Logger.getGlobal().info("I am assuming that the biomass is below 10% the virgin level of 10million");
        biomass = state.getLatestYearlyObservation("Biomass Species 0");
        Logger.getGlobal().info("The actual remaining biomass is: " + biomass);
        Assertions.assertTrue(biomass < 1000000);
    }

}
