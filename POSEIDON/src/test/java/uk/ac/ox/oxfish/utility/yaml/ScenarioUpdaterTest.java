package uk.ac.ox.oxfish.utility.yaml;

import junit.framework.TestCase;
import uk.ac.ox.oxfish.model.scenario.EpoScenario;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;

import static uk.ac.ox.oxfish.utility.yaml.ScenarioUpdater.updateScenario;

public class ScenarioUpdaterTest extends TestCase {

    public void testUpdateScenario() {
        final Path folder = Paths.get("inputs/tests/tunabacksliding/");
        final Consumer<EpoScenario<?, ?>> scenarioConsumer = (scenario) ->
            scenario.getVesselsFile().setPath(Paths.get("tests/backsliding/boats.csv"));

        updateScenario(
            folder.resolve("base_scenario.yaml"),
            folder.resolve("base_scenario_1.yaml"),
            scenarioConsumer
        );
        updateScenario(
            folder.resolve("linear.yaml"),
            folder.resolve("linear_1.yaml"),
            scenarioConsumer
        );
        updateScenario(
            folder.resolve("scenario_logistic.yaml"),
            folder.resolve("scenario_logistic_1.yaml"),
            scenarioConsumer
        );
    }

}