package uk.ac.ox.oxfish.utility.yaml;

import junit.framework.TestCase;
import uk.ac.ox.oxfish.model.scenario.EpoScenario;
import uk.ac.ox.oxfish.model.scenario.InputFile;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static uk.ac.ox.oxfish.utility.yaml.ScenarioUpdater.updateScenario;

public class ScenarioUpdaterTest extends TestCase {

    public void testUpdateScenario() {
        final Path folder = Paths.get("inputs/tests/tunabacksliding/");

        final Function<Stream<String>, String> lineProcessor = lines -> lines
            .filter(line -> !line.matches(".*vesselsFilePath.*"))
            .filter(line -> !line.matches(".*locationValuesFile.*"))
            .filter(line -> !line.matches(".*purseSeineGearFactory.*"))
            .map(line -> line.replace("abundancePurseSeineGearFactory:", "purseSeineGearFactory: !!uk.ac.ox.oxfish.fisher.equipment.gear.factory.AbundancePurseSeineGearFactory"))
            .collect(Collectors.joining("\n"));


        final Consumer<EpoScenario<?, ?>> scenarioConsumer = (scenario) -> {
            scenario.getVesselsFile().setPath(Paths.get("tests/backsliding/boats.csv"));
            scenario.getPurseSeineGearFactory().setLocationValuesFile(
                new InputFile(scenario.getInputFolder(), Paths.get("tests/backsliding/location_values.csv"))
            );
        };

        updateScenario(
            folder.resolve("base_scenario.yaml"),
            folder.resolve("base_scenario_1.yaml"),
            lineProcessor,
            scenarioConsumer
        );
        updateScenario(
            folder.resolve("linear.yaml"),
            folder.resolve("linear_1.yaml"),
            lineProcessor,
            scenarioConsumer
        );
        updateScenario(
            folder.resolve("scenario_logistic.yaml"),
            folder.resolve("scenario_logistic_1.yaml"),
            lineProcessor,
            scenarioConsumer
        );
    }

}