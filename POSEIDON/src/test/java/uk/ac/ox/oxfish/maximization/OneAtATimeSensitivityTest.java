package uk.ac.ox.oxfish.maximization;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.apache.commons.math3.util.Precision.EPSILON;

public class OneAtATimeSensitivityTest {

    @Test
    public void testValueRange() {
        Assertions.assertArrayEquals(
            new double[]{0, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100},
            OneAtATimeSensitivity.valueRange(0, 100, 11).toArray(),
            EPSILON
        );
        Assertions.assertArrayEquals(
            new double[]{25, 35, 45, 55, 65, 75},
            OneAtATimeSensitivity.valueRange(25, 75, 6).toArray(),
            EPSILON
        );
    }
}