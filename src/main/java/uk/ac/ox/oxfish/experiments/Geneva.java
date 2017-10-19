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

import sim.field.grid.IntGrid2D;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A small heatmap drawing experiment for the geneva presentation
 * Created by carrknight on 1/13/16.
 */
public class Geneva {

    public static final Path MAIN_DIRECTORY = Paths.get("docs", "20160107 optimal_mpa");

    public static void main(String[] args) throws IOException {
        FishYAML yaml = new FishYAML();
        String optimalMpa = String.join("\n", Files.readAllLines(
                MAIN_DIRECTORY.resolve("mixed_mpa.yaml")));
        PrototypeScenario scenario = yaml.loadAs(optimalMpa, PrototypeScenario.class);

        FishState state = new FishState(0);
        state.setScenario(scenario);

        state.start();
        double[][] theGrid = new double[state.getMap().getWidth()][state.getMap().getHeight()];

        while(state.getYear()<20)
        {
            state.schedule.step(state);
            IntGrid2D trawls = state.getMap().getDailyTrawlsMap();
            for(int x =0; x<state.getMap().getWidth(); x++)
            {
                for (int y = 0; y < state.getMap().getHeight(); y++)
                {
                    theGrid[x][state.getMap().getHeight()-y-1] += trawls.get(x, y);
                }
            }
        }



        String csvGrid = FishStateUtilities.gridToCSV(theGrid);
        FileWriter writer = new FileWriter(
                MAIN_DIRECTORY.
                        resolve("mpa_grid.csv").toFile());
        writer.write(csvGrid);
        writer.close();
    }
}
