package uk.ac.ox.oxfish.maximization;

import com.beust.jcommander.JCommander;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TwoPunchCalibrationTest {

    @Test
    public void testArgsParsing() {
        final TwoPunchCalibration twoPunchCalibration = new TwoPunchCalibration();
        JCommander.newBuilder()
            .addObject(twoPunchCalibration)
            .build()
            .parse("-p 32 -g 100 -l 200".split("\\s+"));
        Assertions.assertEquals(32, twoPunchCalibration.getParallelThreads());
        Assertions.assertEquals(100, twoPunchCalibration.getMaxGlobalCalls());
        Assertions.assertEquals(200, twoPunchCalibration.getMaxLocalCalls());
    }
}