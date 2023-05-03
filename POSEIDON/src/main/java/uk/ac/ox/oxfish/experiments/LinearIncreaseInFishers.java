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

package uk.ac.ox.oxfish.experiments;

import com.esotericsoftware.minlog.Log;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Create and remove fishers
 * Created by carrknight on 12/14/15.
 */
public class LinearIncreaseInFishers
{

    /**
     * go from 300 fishers back to 0 immediately
     */
    public static void main(String[] args)
    {
        FishState state = new FishState(System.currentTimeMillis());
        Log.set(Log.LEVEL_NONE);

        PrototypeScenario scenario = new PrototypeScenario();
        scenario.setFishers(50);

        //lspiRun the model for a full 3 years before progressing
        state.setScenario(scenario);
        state.start();
        while(state.getYear()<3)
            state.schedule.step(state);

        //now keep running for 10 years adding 5 fishers every month
        while(state.getYear()<13)
        {
            if (state.getDayOfTheYear() % 30 == 0)
            {
                state.createFisher(FishState.DEFAULT_POPULATION_NAME);
                state.createFisher(FishState.DEFAULT_POPULATION_NAME);
                state.createFisher(FishState.DEFAULT_POPULATION_NAME);
                state.createFisher(FishState.DEFAULT_POPULATION_NAME);
                state.createFisher(FishState.DEFAULT_POPULATION_NAME);
            }
            state.schedule.step(state);
        }

        //for the next 10 years remove the fishers
        while(state.getYear()<23)
        {
            while (state.getFishers().size() > 0)
            {
                state.killRandomFisher();
            }
            state.schedule.step(state);
        }

        Path container = Paths.get("runs", "entry-exit");
        container.toFile().mkdirs();
        FishStateUtilities.printCSVColumnsToFile(container.resolve("sample2.csv").toFile(),
                                                 state.getDailyDataSet().getColumn("Number of Fishers"),
                                                 state.getDailyDataSet().getColumn("Species 0 Landings"),
                                                 state.getDailyDataSet().getColumn("Biomass Species 0")
        );

    }

    public static void slowInSlowOut(String[] args)
    {
        FishState state = new FishState(System.currentTimeMillis());
        Log.set(Log.LEVEL_NONE);

        PrototypeScenario scenario = new PrototypeScenario();
        scenario.setFishers(50);

        //lspiRun the model for a full 3 years before progressing
        state.setScenario(scenario);
        state.start();
        while(state.getYear()<3)
            state.schedule.step(state);

        //now keep running for 10 years adding 5 fishers every month
        while(state.getYear()<13)
        {
            if (state.getDayOfTheYear() % 30 == 0)
            {
                state.createFisher(FishState.DEFAULT_POPULATION_NAME);
                state.createFisher(FishState.DEFAULT_POPULATION_NAME);
              //  state.createFisher();
             //   state.createFisher();
             //   state.createFisher();
            }
            state.schedule.step(state);
        }

        //for the next 10 years remove the fishers
        while(state.getYear()<23)
        {
            if (state.getDayOfTheYear() % 30 == 0)
            {
                state.killRandomFisher();
                state.killRandomFisher();
            //    state.killRandomFisher();
            //    state.killRandomFisher();
            //    state.killRandomFisher();
            }
            state.schedule.step(state);
        }

        Path container = Paths.get("runs", "entry-exit");
        container.toFile().mkdirs();
        FishStateUtilities.printCSVColumnsToFile(container.resolve("sample.csv").toFile(),
                                                 state.getDailyDataSet().getColumn("Number of Fishers"),
                                                 state.getDailyDataSet().getColumn("Species 0 Landings"),
                                                 state.getDailyDataSet().getColumn("Biomass Species 0")
                                                 );

    }



}
