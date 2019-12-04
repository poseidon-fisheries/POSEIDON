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
                9.977,-8.007,-9.139,-10.000,-7.752,-0.713, 3.046, 9.466, 9.621,-5.917,-9.259, 8.974,-10.000
        };

        FishYAML yaml = new FishYAML();
        Path optimizationFile = Paths.get("inputs", "tuna", "calibration_landingsonly.yaml");
        GenericOptimization optimization = yaml.loadAs(new FileReader(optimizationFile.toFile()), GenericOptimization.class);

//        GenericOptimization.buildLocalCalibrationProblem(
//                optimizationFile,
//                optimalParameters,
//                "calibration_betty_local.yaml",
//                .2
//
//        );


        Scenario scenario = optimization.buildScenario(optimalParameters);
        Path outputFile = optimizationFile.getParent().resolve("tuna_landingsonly_calibrated.yaml");
        yaml.dump(scenario, new FileWriter(outputFile.toFile()));

        ScaledFixedDataLastStepTarget.VERBOSE = true;
        optimization.evaluate(optimalParameters);

    }



}
