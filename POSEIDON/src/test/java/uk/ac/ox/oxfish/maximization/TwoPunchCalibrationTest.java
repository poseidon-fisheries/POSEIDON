package uk.ac.ox.oxfish.maximization;

import com.beust.jcommander.JCommander;
import junit.framework.TestCase;

public class TwoPunchCalibrationTest extends TestCase {

    public void testArgsParsing() {
        final TwoPunchCalibration twoPunchCalibration = new TwoPunchCalibration();
        JCommander.newBuilder()
            .addObject(twoPunchCalibration)
            .build()
            .parse("-p 32 -g 100 -l 200".split("\\s+"));
        assertEquals(32, twoPunchCalibration.getParallelThreads());
        assertEquals(100, twoPunchCalibration.getMaxGlobalCalls());
        assertEquals(200, twoPunchCalibration.getMaxLocalCalls());
    }
}