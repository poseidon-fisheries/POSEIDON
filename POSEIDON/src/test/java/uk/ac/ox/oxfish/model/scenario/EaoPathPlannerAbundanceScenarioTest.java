package uk.ac.ox.oxfish.model.scenario;

import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.maximization.TunaEvaluator;
import uk.ac.ox.oxfish.model.FishState;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static uk.ac.ox.oxfish.model.scenario.TestableScenario.startTestableScenario;

class EaoPathPlannerAbundanceScenarioTest {
    @Test
    void runOneYearWithoutCrashing() {
        final FishState fishState = startTestableScenario(EaoPathPlannerAbundanceScenario.class);
        do {
//            System.out.println("Day "+fishState.getStep());
            fishState.schedule.step(fishState);
        } while (fishState.getYear() < 1);
    }

    @Test
    void runThreeYearsWithoutCrashing() {
        final FishState fishState = startTestableScenario(EaoPathPlannerAbundanceScenario.class);
        do {
            fishState.schedule.step(fishState);
        } while (fishState.getYear() < 3);
    }

    @Test
    void evaluatorTest(){
        final Path testScenario = Paths.get(("D:/MARELA/eao_calibrations/test_calibration/calibration.yaml"));
        final double[] zeros = new double[29]; // hard coded for laziness and debugging
        Arrays.fill(zeros, 0d);
        final TunaEvaluator evaluator = new TunaEvaluator(testScenario, zeros);
        evaluator.setNumRuns(1);
        evaluator.setParallel(false);
        evaluator.run();
    }
}
