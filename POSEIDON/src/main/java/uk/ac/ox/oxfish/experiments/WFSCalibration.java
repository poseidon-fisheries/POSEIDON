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

package uk.ac.ox.oxfish.experiments;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by carrknight on 5/29/17.
 */
public class WFSCalibration {


    public static final int RUNS = 10000;
    public static final Path MAIN_DIRECTORY = Paths.get("docs", "20170526 gom_catchability", "results");
    public static final int YEARS_PER_RUN = 3;

    public static void main(String[] args) throws IOException {
        //runMultipleTimesToBuildHistogram("best_scalable");
        //runMultipleTimesToBuildHistogram("best_total");
        runMultipleTimesToBuildHistogram("best_total_800");


    }

    private static void runMultipleTimesToBuildHistogram(final String input) throws IOException {
        //write header
        FileWriter writer = new FileWriter(MAIN_DIRECTORY.resolve(input + ".csv").toFile());
        writer.write("average_profits,RedSnapper,RedGrouper,GagGrouper");
        writer.write("\n");
        writer.flush();


        for (int run = 0; run < RUNS; run++) {

            FishYAML yaml = new FishYAML();
            Scenario scenario = yaml.loadAs(
                new FileReader(MAIN_DIRECTORY.resolve(input + ".yaml").toFile()),
                Scenario.class
            );

            FishState state = new FishState(run);
            state.setScenario(scenario);

            //run the model
            state.start();
            while (state.getYear() < YEARS_PER_RUN)
                state.schedule.step(state);
            state.schedule.step(state);

            writer.write(
                state.getLatestYearlyObservation("Average Cash-Flow") + "," +
                    state.getLatestYearlyObservation("Last Season Day of RedSnapper") + "," +
                    state.getLatestYearlyObservation("RedGrouper Landings") + "," +
                    state.getLatestYearlyObservation("GagGrouper Landings") + "\n"
            );


            writer.flush();


        }

        writer.close();
    }

}
