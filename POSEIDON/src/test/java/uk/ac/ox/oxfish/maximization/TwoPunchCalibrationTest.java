package uk.ac.ox.oxfish.maximization;

import com.beust.jcommander.JCommander;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

public class TwoPunchCalibrationTest {

    @Test
    public void testArgsParsing() {
        final TwoPunchCalibration twoPunchCalibration = new TwoPunchCalibration();
        JCommander.newBuilder()
            .addObject(twoPunchCalibration)
            .build()
            .parse("-p 32 -g 100 -l 200".split("\\s+"));
        Assert.assertEquals(32, twoPunchCalibration.getParallelThreads());
        Assert.assertEquals(100, twoPunchCalibration.getMaxGlobalCalls());
        Assert.assertEquals(200, twoPunchCalibration.getMaxLocalCalls());
    }
}