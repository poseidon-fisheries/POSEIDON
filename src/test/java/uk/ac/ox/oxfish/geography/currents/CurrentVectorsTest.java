package uk.ac.ox.oxfish.geography.currents;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static uk.ac.ox.oxfish.Main.STEPS_PER_DAY;

public class CurrentVectorsTest {

    @Test
    public void positiveDaysOffset() {
        final CurrentVectors currentVectors = new CurrentVectors(null, null, STEPS_PER_DAY);
        assertEquals(0, currentVectors.positiveDaysOffset(1, 1));
        assertEquals(1, currentVectors.positiveDaysOffset(1, 2));
        assertEquals(364, currentVectors.positiveDaysOffset(1, 365));
        assertEquals(1, currentVectors.positiveDaysOffset(365, 1));
    }

    @Test
    public void negativeDaysOffset() {
        final CurrentVectors currentVectors = new CurrentVectors(null, null, STEPS_PER_DAY);
        assertEquals(0, currentVectors.negativeDaysOffset(1, 1));
        assertEquals(-1, currentVectors.negativeDaysOffset(2, 1));
        assertEquals(-364, currentVectors.negativeDaysOffset(365, 1));
        assertEquals(-1, currentVectors.negativeDaysOffset(1, 365));
    }
}