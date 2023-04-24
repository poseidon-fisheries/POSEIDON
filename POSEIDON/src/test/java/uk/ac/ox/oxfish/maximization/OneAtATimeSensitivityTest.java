package uk.ac.ox.oxfish.maximization;

import junit.framework.TestCase;

import static org.apache.commons.math3.util.Precision.EPSILON;
import static org.junit.Assert.assertArrayEquals;

public class OneAtATimeSensitivityTest extends TestCase {

    public void testValueRange() {
        assertArrayEquals(
            new double[]{0, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100},
            OneAtATimeSensitivity.valueRange(0, 100, 11).toArray(),
            EPSILON
        );
        assertArrayEquals(
            new double[]{25, 35, 45, 55, 65, 75},
            OneAtATimeSensitivity.valueRange(25, 75, 6).toArray(),
            EPSILON
        );
    }
}