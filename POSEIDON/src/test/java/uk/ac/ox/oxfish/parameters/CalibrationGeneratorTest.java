package uk.ac.ox.oxfish.parameters;

import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.model.scenario.EpoPathPlannerAbundanceScenario;

import java.nio.file.Path;
import java.nio.file.Paths;

public class CalibrationGeneratorTest {

    @Test
    public void testGenerateCalibration() {
        final Path calibrationFolder = Paths.get("inputs", "epo_inputs", "calibration");
        new CalibrationGenerator().generateCalibration(
            new EpoPathPlannerAbundanceScenario(),
            calibrationFolder,
            calibrationFolder.resolve("calibration_targets.csv"),
            2017,
            1,
            2
        );
    }
}