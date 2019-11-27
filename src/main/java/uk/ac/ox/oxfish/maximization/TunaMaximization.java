package uk.ac.ox.oxfish.maximization;

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
                6.075, -2.655, -0.393, -5.264, -3.480, -1.399, -3.216, 1.140, 0.344
        };


        FishYAML yaml = new FishYAML();
        Path optimizationFile = Paths.get("inputs", "tuna", "calibration.yaml");
        GenericOptimization optimization = yaml.loadAs(new FileReader(optimizationFile.toFile()), GenericOptimization.class);

        Scenario scenario = optimization.buildScenario(optimalParameters);
        Path outputFile = optimizationFile.getParent().resolve("tuna_virgin_calibrated.yaml");
        yaml.dump(scenario, new FileWriter(outputFile.toFile()));


        optimization.evaluate(optimalParameters);

    }
}
