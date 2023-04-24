package uk.ac.ox.oxfish.maximization;

import junit.framework.TestCase;

import static org.apache.commons.math3.util.Precision.EPSILON;
import static org.junit.Assert.assertArrayEquals;

public class OneAtATimeSensitivityTest extends TestCase {

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