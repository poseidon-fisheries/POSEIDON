package uk.ac.ox.oxfish.maximization;

import junit.framework.TestCase;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.apache.commons.math3.util.Precision.EPSILON;
import static org.junit.Assert.assertArrayEquals;

public class OneAtATimeSensitivityTest extends TestCase {

    private final Path calibrationFolder = Paths.get(
        System.getProperty("user.home"),
        "workspace", "tuna",
        "np", "calibrations",
        "2023_april_run/cenv0729/2023-04-20_14.32.52_local"
    );

    public void testRun() {
        final OneAtATimeSensitivity oneAtATimeSensitivity =
            new OneAtATimeSensitivity(
                calibrationFolder.resolve("calibration.yaml"),
                calibrationFolder.resolve("calibration_log.md"),
                calibrationFolder.resolve("sensitivity"),
                5,
                8,
                3
            );
        oneAtATimeSensitivity.run();
    }

    public void testValueRange() {
        assertArrayEquals(
            new double[]{0, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100},
            OneAtATimeSensitivity.valueRange(50, 0, 100, 0, 2, 11).toArray(),
            EPSILON
        );
        assertArrayEquals(
            new double[]{25, 35, 45, 55, 65, 75},
            OneAtATimeSensitivity.valueRange(50, 25, 75, 0, 2, 6).toArray(),
            EPSILON
        );
    }
}