package uk.ac.ox.oxfish.parameters;

import junit.framework.TestCase;
import uk.ac.ox.oxfish.model.scenario.EpoPathPlanningAbundanceScenario;

import java.nio.file.Path;
import java.nio.file.Paths;

public class CalibrationGeneratorTest extends TestCase {

    public void testGenerateCalibration() {
        final Path calibrationFolder = Paths.get("inputs", "epo_inputs", "calibration");
        new CalibrationGenerator().generateCalibration(
            new EpoPathPlanningAbundanceScenario(),
            calibrationFolder,
            calibrationFolder.resolve("calibration_targets.csv"),
            2017,
            1,
            2
        );
    }
}