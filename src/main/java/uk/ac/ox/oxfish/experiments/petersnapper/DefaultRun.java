package uk.ac.ox.oxfish.experiments.petersnapper;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.FlexibleScenario;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;

public class DefaultRun {

    private static final Path SCENARIO_PATH = Paths.get("temp", "peter_snapper", "scenario.yaml");

    public static void main(String[] args) {
        FlexibleScenario scenario = loadScenario(SCENARIO_PATH, FlexibleScenario.class);
        LinkedHashMap<String, Integer> initialFishersPerPort = scenario.getFisherDefinitions().get(0).getInitialFishersPerPort();
        initialFishersPerPort.put("Benoa", 1);
        initialFishersPerPort.put("Kupang", 0);
        FishState state = new FishState(0);
        state.setScenario(scenario);
        state.start();
        while (state.getYear() < 10)
            state.schedule.step(state);

        System.out.println(state.getYearlyDataSet().getColumn("Peter Snapper Landings").toString());
    }

    private static <T extends Scenario> T loadScenario(final Path scenarioPath, final Class<T> scenarioClass) {
        try (FileReader fileReader = new FileReader(scenarioPath.toFile())) {
            final FishYAML fishYAML = new FishYAML();
            return fishYAML.loadAs(fileReader, scenarioClass);
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("Can't find scenario file: " + SCENARIO_PATH, e);
        } catch (IOException e) {
            throw new IllegalStateException("Error while reading file: " + SCENARIO_PATH, e);
        }
    }
}
