package uk.ac.ox.oxfish.model.scenario;

import com.vividsolutions.jts.geom.Coordinate;
import org.junit.jupiter.api.Test;
import sim.field.continuous.Continuous2D;
import sim.util.Bag;
import sim.util.Double2D;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.Fad;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.maximization.TunaEvaluator;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.fxcollections.ObservableList;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager.getFadManager;
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
        final Path testScenario = Paths.get(("D:/MARELA/atl_inputs/calibration/calibration.yaml"));
        final double[] zeros = new double[30]; // hard coded for laziness and debugging
        Arrays.fill(zeros, 0d);
        final TunaEvaluator evaluator = new TunaEvaluator(testScenario, zeros);
        evaluator.setSaveAnimation(true);
        evaluator.setNumRuns(1);
        evaluator.setParallel(false);
        evaluator.run();
    }

    @Test
    void evaluatorTestWithAnimation(){
        final Path testScenario = Paths.get(("D:/MARELA/atl_inputs/calibration/calibration.yaml"));
        final double[] zeros = new double[30]; // hard coded for laziness and debugging
        Arrays.fill(zeros, 0d);
        final TunaEvaluator evaluator = new TunaEvaluator(testScenario, zeros);
        evaluator.setSaveAnimation(true);
        evaluator.setNumRuns(1);
        evaluator.setParallel(false);
        evaluator.run();
    }

    @Test
    void runOneYearOutputDailySnapShots() {
        final FishState fishState = startTestableScenario(EaoPathPlannerAbundanceScenario.class);
        do {
//            System.out.println("Day "+fishState.getStep());
            fishState.schedule.step(fishState);
            int stepNum = fishState.getStep();
            if(fishState.getFadMap().getDriftingObjectsMap().getField().getAllObjects().size()>0){
                ObservableList<Fisher> fishers = fishState.getFishers();
                Bag fads = fishState.getFadMap().getDriftingObjectsMap().getField().getAllObjects();
                Object[] allFads = fishState.getFadMap().allFads().toArray();

                Continuous2D driftingObjectField = fishState.getFadMap().getField();

 //               Map hash = driftingObjectField.doubleLocationHash;

                Object[] streamFads =
                    fishState.getFadMap().allFads().map(Fad::getCoordinate).toArray();
                
                Optional<Double2D> fadloc = fishState.getFadMap().getFadLocation((Fad) allFads[0]);
                System.out.println("Day "+fishState.getStep());
            }

        } while (fishState.getYear() < 2);
    }

}
