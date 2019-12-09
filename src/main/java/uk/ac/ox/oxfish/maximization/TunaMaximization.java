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
//                -2.214, 8.405, 8.737, 10.000,-2.964, 0.172, 10.000,-9.659,-0.100,-8.300
                0.30787210787752545, 2.4263145384058884, -9.841376719101739, 9.83753901741901, 4.29913654405374, 9.182297691041107, 7.489078093360722, -0.46308912132890234, 8.251653931164888, -9.542987973280287

        };

        FishYAML yaml = new FishYAML();
        //Path optimizationFile = Paths.get("inputs", "tuna", "calibration_landingsonly_grav.yaml");
        Path optimizationFile = Paths.get("inputs", "tuna", "calibration_alltargets_grav_local2.yaml");
        GenericOptimization optimization = yaml.loadAs(new FileReader(optimizationFile.toFile()), GenericOptimization.class);

        GenericOptimization.buildLocalCalibrationProblem(
                optimizationFile,
                optimalParameters,
                "calibration_alltargets_grav_local3.yaml",
                .2

        );


        Scenario scenario = optimization.buildScenario(optimalParameters);
        Path outputFile = optimizationFile.getParent().resolve("tuna_alltargets_grav_local.yaml");
        yaml.dump(scenario, new FileWriter(outputFile.toFile()));

        ScaledFixedDataLastStepTarget.VERBOSE = true;
        optimization.evaluate(optimalParameters);

    }



}
