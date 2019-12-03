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
                -1.868,-10.000, 3.664,-10.000, 9.338,-5.807, 10.000,-6.909,-6.281,-6.881,-10.000,-4.382,-9.100
        };

        FishYAML yaml = new FishYAML();
        Path optimizationFile = Paths.get("inputs", "tuna", "calibration_betty.yaml");
        GenericOptimization optimization = yaml.loadAs(new FileReader(optimizationFile.toFile()), GenericOptimization.class);

        GenericOptimization.buildLocalCalibrationProblem(
                optimizationFile,
                optimalParameters,
                "calibration_betty_local.yaml",
                .2

        );


        Scenario scenario = optimization.buildScenario(optimalParameters);
        Path outputFile = optimizationFile.getParent().resolve("tuna_betty_calibrated.yaml");
        yaml.dump(scenario, new FileWriter(outputFile.toFile()));

        ScaledFixedDataLastStepTarget.VERBOSE = true;
        optimization.evaluate(optimalParameters);

    }



}
