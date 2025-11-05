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
        final Path testScenario = Paths.get(("D:/MARELA/atl_inputs/calibration/local_calibration.yaml"));
        final double[] zeros = new double[29]; // hard coded for laziness and debugging
        Arrays.fill(zeros, 0d);
        //a local solution... replace
        final double[] localSolution = {7.673872269729801,-24.76488022210531,5.615956175264788,-73.22220336657873,-85.75279743280186,86.28098304630247,-47.758872305019764,9.387159766283602,34.01310151386688,1.296119890365274,31.40692440273719,36.782786641865926,27.684061429521662,72.58023019451645,10.755521352678475,-34.55047047964018,14.809286518585411,-212.2120515641607,-5.652453274545319,21.216156888822663,-3.0952043996392398,-4.96365417237626,-53.366160768650595,85.68355427958024,244.00419371478372,11.06701838569846,-11.120687575242098,29.30505979398604,30.824609772630346};

        final TunaEvaluator evaluator = new TunaEvaluator(testScenario, localSolution);
        evaluator.setSaveAnimation(false);
        evaluator.setWriteFadFates(false);
        evaluator.setSimEventsAllYears(false);
        evaluator.setNumRuns(10);
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


}
