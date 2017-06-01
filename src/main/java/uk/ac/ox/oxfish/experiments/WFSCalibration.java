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

    public static void  main(String[] args) throws IOException {
        //runMultipleTimesToBuildHistogram("best_scalable");
        runMultipleTimesToBuildHistogram("best_total");



    }

    private static void runMultipleTimesToBuildHistogram(final String input) throws IOException {
        //write header
        FileWriter writer = new FileWriter(MAIN_DIRECTORY.resolve(input + ".csv").toFile());
        writer.write("average_profits,RedSnapper,RedGrouper,GagGrouper");
        writer.write("\n");
        writer.flush();


        for(int run = 0; run<RUNS; run++)
        {

            FishYAML yaml  = new FishYAML();
            Scenario scenario = yaml.loadAs(new FileReader(MAIN_DIRECTORY.resolve(input + ".yaml").toFile()),
                                            Scenario.class);

            FishState state = new FishState(run);
            state.setScenario(scenario);

            //run the model
            state.start();
            while(state.getYear()< YEARS_PER_RUN)
                state.schedule.step(state);
            state.schedule.step(state);

            writer.write(
                    state.getLatestYearlyObservation("Average Cash-Flow") + ","+
                            state.getLatestYearlyObservation("Last Season Day of RedSnapper") + ","+
                            state.getLatestYearlyObservation("RedGrouper Landings") + ","+
                            state.getLatestYearlyObservation("GagGrouper Landings") +"\n"
            );


            writer.flush();


        }

        writer.close();
    }

}
