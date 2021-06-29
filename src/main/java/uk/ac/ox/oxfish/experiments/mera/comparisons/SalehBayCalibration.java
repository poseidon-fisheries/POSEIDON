package uk.ac.ox.oxfish.experiments.mera.comparisons;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static uk.ac.ox.oxfish.experiments.mera.comparisons.MeraFakeOMHotstarts.checkError;

public class SalehBayCalibration {


    public static final int SCENARIOS_TO_RUN = 100;

    static Path MAIN_DIRECTORY = Paths.get("docs/mera_hub/diding");

    public static void main(String[] args) throws IOException {
        //calibration(args);
        createOptimizationMasterlist();
    }

    private static void createOptimizationMasterlist() throws IOException {
        //create list of scenario runs
        Path scenarioList = MAIN_DIRECTORY.resolve("results").resolve("scenarios").resolve("scenario_list.csv");
        final FileWriter writer = new FileWriter(scenarioList.toFile());
        writer.write("scenario,year");
        writer.flush();
        for (int hotstart = 1; hotstart < SCENARIOS_TO_RUN; hotstart++){
            System.out.println(hotstart);
            final Path optimized = MAIN_DIRECTORY.resolve("hotstarts_three").resolve(String.valueOf(hotstart)).resolve("optimized.yaml");
            if(optimized.toFile().exists()) {
                if (checkError(optimized) < 10) {
                    System.out.println("Accepted!");
                    writer.write("\n");
                    writer.write(optimized.toString() + ",2");
                    writer.flush();
                } else {
                    System.out.println("Rejected!");
                }
            }
        }
        writer.close();
    }


    private static void calibration(String[] args) throws IOException {
        int startingScenario = 0;
        int scenariosToRun = SCENARIOS_TO_RUN;
        if(args.length == 1) {
            startingScenario = Integer.parseInt(args[0]);
            scenariosToRun = startingScenario + 20;
        }
        //calibration
        for (int hotstart = startingScenario; hotstart < scenariosToRun; hotstart++) {
            MeraFakeOMHotstarts.calibrate(MAIN_DIRECTORY.resolve("hotstarts_three").
                    resolve(String.valueOf(hotstart)).resolve("optimization.yaml"), 50, 20, 3);
        }
    }


}
