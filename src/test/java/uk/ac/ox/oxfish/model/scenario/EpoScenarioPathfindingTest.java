package uk.ac.ox.oxfish.model.scenario;

import junit.framework.TestCase;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileReader;
import java.nio.file.Paths;

import static org.junit.Assert.*;
import static uk.ac.ox.oxfish.model.scenario.TestableScenario.startTestableScenario;

public class EpoScenarioPathfindingTest extends TestCase {

    public void testEpoScenarioPathfinding() throws Exception{
        FishYAML yaml = new FishYAML();

        final EpoScenarioPathfinding scenario = yaml.loadAs(
                new FileReader(
                        Paths.get("inputs","epo_inputs","calibration","EPO_dev_scenario_BP.yaml").toFile()),
                EpoScenarioPathfinding.class
        );
        final FishState fishState = new FishState();
        fishState.setScenario(scenario);
        do {
            fishState.schedule.step(fishState);
        } while (fishState.getYear() < 1);
    }
}
