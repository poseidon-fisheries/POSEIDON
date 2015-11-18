package uk.ac.ox.oxfish.model.scenario;

import org.junit.Test;
import uk.ac.ox.oxfish.experiments.Dashboard;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.nio.file.Files;

import static org.junit.Assert.assertEquals;

/**
 * Created by carrknight on 11/18/15.
 */
public class PrototypeScenarioTest {

    @Test
    public void startingMPAs() throws Exception {


        //make sure we can add MPAs from list
        String override = "Prototype:\n" +
                "  startingMPAs:\n" +
                "  - height: 6\n" +
                "    topLeftX: 0\n" +
                "    topLeftY: 0\n" +
                "    width: 5\n" +
                "  - height: 5\n" +
                "    topLeftX: 10\n" +
                "    topLeftY: 10\n" +
                "    width: 5\n";

        //read in the base scenario
        String baseScenario = String.join("\n", Files.readAllLines(Dashboard.DASHBOARD_INPUT_DIRECTORY.resolve("base.yaml")));

        String fullScenario = override + "\n" + baseScenario;
        FishYAML yaml = new FishYAML();
        PrototypeScenario scenario = yaml.loadAs(fullScenario, PrototypeScenario.class);

        assertEquals(scenario.getStartingMPAs().size(),2);
        //the order can be flipped
        assertEquals(scenario.getStartingMPAs().get(0).getHeight(),5,1);
        assertEquals(scenario.getStartingMPAs().get(1).getHeight(),5,1);


    }
}