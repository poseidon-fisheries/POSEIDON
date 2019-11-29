package uk.ac.ox.oxfish.maximization;

import uk.ac.ox.oxfish.maximization.generic.ScaledFixedDataLastStepTarget;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TunaMaximization {

    /**
     * For now, just generate a new tuna scenario file (tuna.yaml) from the copy-pasted parameters
     * resulting from running calibration.yaml.
     */
    public static void main(String[] args) throws IOException {

        double[] optimalParameters = {
                8.337,-1.984, 2.302,-3.536,-7.273, 10.000, 2.877,-7.130,-8.238
        };


        FishYAML yaml = new FishYAML();
        Path optimizationFile = Paths.get("inputs", "tuna", "calibration.yaml");
        GenericOptimization optimization = yaml.loadAs(new FileReader(optimizationFile.toFile()), GenericOptimization.class);

        Scenario scenario = optimization.buildScenario(optimalParameters);
        Path outputFile = optimizationFile.getParent().resolve("tuna_calibrated.yaml");
        yaml.dump(scenario, new FileWriter(outputFile.toFile()));


        ScaledFixedDataLastStepTarget.VERBOSE=true;
        optimization.evaluate(optimalParameters);

    }
}
