package uk.ac.ox.oxfish.experiments;

import com.esotericsoftware.minlog.Log;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Runs social tuning many times, looking for the right parameter
 * Created by carrknight on 8/30/16.
 */
public class SocialTuningExercise {


    private final static int YEARS_TO_RUN = 5;
    public static final Path MAIN_DIRECTORY = Paths.get("runs", "social_tuning");
    public static final int NUMBER_OF_EXPERIMENTS = 100;

    public static void nn(String[] args) throws IOException {


        StringBuilder output = new StringBuilder("time,x,y,distance,habitat,cash");
        Log.set(Log.LEVEL_INFO);
        String inputScenario =  String.join("\n", Files.readAllLines(
                MAIN_DIRECTORY.resolve("nn.yaml")));
        for(int experiment = 1; experiment< NUMBER_OF_EXPERIMENTS; experiment++)
        {
            Log.info("Starting experiment " + experiment);
            FishYAML yaml = new FishYAML();
            PrototypeScenario scenario = yaml.loadAs(inputScenario, PrototypeScenario.class);

            FishState state = new FishState(experiment);
            state.setScenario(scenario);
            state.start();
            while(state.getYear()<YEARS_TO_RUN)
                state.schedule.step(state);
            output.append("\n");
            for(int i=0; i<5; i++) {
                output.append(
                        state.getYearlyDataSet().getLatestObservation("Average Heatmap Parameter "+i)
                ).append(",");

            }

            double total = 0;
            for(double cash : state.getYearlyDataSet().getColumn("Average Cash-Flow"))
                total+=cash;
            output.append(total);


        }

        Files.write(MAIN_DIRECTORY.resolve("nn.csv"), output.toString().getBytes());





    }



    public static void kernel(String[] args) throws IOException {


        StringBuilder output = new StringBuilder("x,y,distance,habitat");
        Log.set(Log.LEVEL_INFO);
        String inputScenario =  String.join("\n", Files.readAllLines(
                MAIN_DIRECTORY.resolve("kernel.yaml")));
        for(int experiment = 1; experiment< NUMBER_OF_EXPERIMENTS; experiment++)
        {
            Log.info("Starting experiment " + experiment);
            FishYAML yaml = new FishYAML();
            PrototypeScenario scenario = yaml.loadAs(inputScenario, PrototypeScenario.class);

            FishState state = new FishState(experiment);
            state.setScenario(scenario);
            state.start();
            while(state.getYear()<YEARS_TO_RUN)
                state.schedule.step(state);
            output.append("\n");
            for(int i=0; i<4; i++) {
                output.append(
                        state.getYearlyDataSet().getLatestObservation("Average Heatmap Parameter "+i)
                ).append(",");

            }

            double total = 0;
            for(double cash : state.getYearlyDataSet().getColumn("Average Cash-Flow"))
                total+=cash;
            output.append(total);


        }

        Files.write(MAIN_DIRECTORY.resolve("kernel.csv"), output.toString().getBytes());





    }


    public static void main(String[] args) throws IOException {


        StringBuilder output = new StringBuilder("distance,evidence,drift,optimism,penalty");
        Log.set(Log.LEVEL_INFO);
        String inputScenario =  String.join("\n", Files.readAllLines(
                MAIN_DIRECTORY.resolve("kalman.yaml")));
        for(int experiment = 1; experiment< NUMBER_OF_EXPERIMENTS; experiment++)
        {
            Log.info("Starting experiment " + experiment);
            FishYAML yaml = new FishYAML();
            PrototypeScenario scenario = yaml.loadAs(inputScenario, PrototypeScenario.class);

            FishState state = new FishState(experiment);
            state.setScenario(scenario);
            state.start();
            while(state.getYear()<YEARS_TO_RUN)
                state.schedule.step(state);
            output.append("\n");
            for(int i=0; i<5; i++) {
                output.append(
                        state.getYearlyDataSet().getLatestObservation("Average Heatmap Parameter "+i)
                ).append(",");

            }

            double total = 0;
            for(double cash : state.getYearlyDataSet().getColumn("Average Cash-Flow"))
                total+=cash;
            output.append(total);


        }

        Files.write(MAIN_DIRECTORY.resolve("kalman.csv"), output.toString().getBytes());





    }

}
