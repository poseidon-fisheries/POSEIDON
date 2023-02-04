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
import uk.ac.ox.oxfish.model.regs.factory.AnarchyFactory;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by carrknight on 4/7/17.
 */
public class ClimateChange {


    private static final Path[] anarchyFiles = new Path[]
            {
                    Paths.get("docs", "20170407 climate", "base","base.yaml"),
                    Paths.get("docs", "20170407 climate", "climate","climate.yaml")
            };

    public static void main(String[] args) throws FileNotFoundException {


        //anarchy files
        for (Path file : anarchyFiles) {
            Log.info("Starting " + file.getFileName());
            FishYAML yaml = new FishYAML();
            PrototypeScenario scenario = yaml.loadAs(new FileReader(file.toFile()),
                                            PrototypeScenario.class);
            scenario.setRegulation(new AnarchyFactory());
            FishState state = new FishState(0l);
            state.setScenario(scenario);
            state.start();
            while (state.getYear() < 20)
                state.schedule.step(state);

            FishStateUtilities.printCSVColumnsToFile(
                    file.getParent().resolve(
                            file.getName(file.getNameCount()-1).toString().split("\\.")[0] +
                                    "_anarchy.csv").toFile(),
                    state.getYearlyDataSet().getColumn("Biomass Species 0"),
                    state.getYearlyDataSet().getColumn("Species 0 Landings"),
                    state.getYearlyDataSet().getColumn("Average Cash-Flow"),
                    state.getYearlyDataSet().getColumn("Total Effort")
            );

        }
    }


    }
