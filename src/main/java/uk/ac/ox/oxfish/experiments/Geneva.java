package uk.ac.ox.oxfish.experiments;

import sim.field.grid.IntGrid2D;
import uk.ac.ox.oxfish.experiments.dedicated.habitat.PolicyAndLocations;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
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



        String csvGrid = PolicyAndLocations.gridToCSV(theGrid);
        FileWriter writer = new FileWriter(
                MAIN_DIRECTORY.
                        resolve("mpa_grid.csv").toFile());
        writer.write(csvGrid);
        writer.close();
    }
}
