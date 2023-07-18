package uk.ac.ox.oxfish.maximization;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static uk.ac.ox.oxfish.utility.FishStateUtilities.EPSILON;

public class SolutionExtractorTest {

    private final Path logFilePath = Paths.get("inputs", "tests", "calibration_log.md");
    private final SolutionExtractor solutionExtractor = new SolutionExtractor(logFilePath);

    @Test
    public void testBestSolution() {
        Assertions.assertArrayEquals(new double[]{
            6.303, 31.043, -52.97, 9.185, 13.916, 1.537, 11.272, 3.304, -26.59, -4.809, -1.293, 8.482, -11.425,
            28.727, -4.879, 29.253, -2.277, -7.442, -9.237, 12.688, -3.326, 14.285, -15.47, -26.264, 39.457, -5.87,
            28.241, -39.562, 6.394, -16.373, 21.936
        }, solutionExtractor.bestSolution().getKey(), EPSILON);
    }

    @Test
    public void testAllSolutions() {
        Assertions.assertEquals(80, solutionExtractor.allSolutions().size());
    }
}