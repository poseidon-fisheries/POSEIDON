package uk.ac.ox.oxfish.maximization;

import uk.ac.ox.oxfish.maximization.generic.FixedDataLastStepTarget;
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
            1.908, 0.380, -10.000, -9.988, 7.437, 2.922, 9.658, 4.225, -9.930, 5.809, 9.925, 4.132, -10.000
        };

        FishYAML yaml = new FishYAML();
        Path optimizationFile = Paths.get("/home/nicolas/workspace/tuna/np/calibrations/2019-12-04_1-full_calibration_with_fixed_resetter", "calibration.yaml");
        GenericOptimization optimization = yaml.loadAs(new FileReader(optimizationFile.toFile()), GenericOptimization.class);

//        GenericOptimization.buildLocalCalibrationProblem(
//                optimizationFile,
//                optimalParameters,
//                "calibration_betty_local.yaml",
//                .2
//
//        );

        Scenario scenario = optimization.buildScenario(optimalParameters);
        Path outputFile = optimizationFile.getParent().resolve("tuna_calibrated.yaml");
        yaml.dump(scenario, new FileWriter(outputFile.toFile()));

        ScaledFixedDataLastStepTarget.VERBOSE = true;
        FixedDataLastStepTarget.VERBOSE = true;
        optimization.evaluate(optimalParameters);

    }

}
