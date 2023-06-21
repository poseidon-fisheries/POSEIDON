package uk.ac.ox.poseidon.r;

import uk.ac.ox.poseidon.common.Services;
import uk.ac.ox.poseidon.simulations.api.FileScenarioLoader;
import uk.ac.ox.poseidon.simulations.api.Scenario;

import java.nio.file.Path;
import java.nio.file.Paths;

public class YamlScenarioLoader {

    public Scenario load(final String scenarioPath) {
        return load(Paths.get(scenarioPath));
    }

    public Scenario load(final Path scenarioPath) {
        return Services
            .loadFirst(
                FileScenarioLoader.class,
                scenarioLoader -> scenarioLoader.getSupportedExtensions().contains("yaml")
            )
            .load(scenarioPath);
    }
}
