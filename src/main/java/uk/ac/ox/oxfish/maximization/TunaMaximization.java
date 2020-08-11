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
//
                -4.524, 8.902, 4.317, 10.000,-0.211,-2.266, 6.876,-9.686,-10.000,-8.192, 3.458
        };

        FishYAML yaml = new FishYAML();
        //Path optimizationFile = Paths.get("inputs", "tuna", "calibration_landingsonly_grav.yaml");
        Path optimizationFile = Paths.get("inputs", "tuna", "calibration_grav.yaml");
        GenericOptimization optimization = yaml.loadAs(new FileReader(optimizationFile.toFile()), GenericOptimization.class);

        GenericOptimization.buildLocalCalibrationProblem(
                optimizationFile,
                optimalParameters,
                "summer_calibrationproblem_grav_local.yaml",
                .2

        );


        Scenario scenario = GenericOptimization.buildScenario(optimalParameters, Paths.get(optimization.getScenarioFile()).toFile(), optimization.getParameters());
        Path outputFile = optimizationFile.getParent().resolve("summer_calibration_grav.yaml");
        yaml.dump(scenario, new FileWriter(outputFile.toFile()));

        ScaledFixedDataLastStepTarget.VERBOSE = true;
        FixedDataLastStepTarget.VERBOSE=true;
        optimization.evaluate(optimalParameters);

    }



}
