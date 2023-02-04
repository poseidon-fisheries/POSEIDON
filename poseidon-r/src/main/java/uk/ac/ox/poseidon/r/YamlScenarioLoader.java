package uk.ac.ox.poseidon.r;

import uk.ac.ox.poseidon.simulation.api.FileScenarioLoader;
import uk.ac.ox.poseidon.simulation.api.Scenario;

import java.nio.file.Path;
import java.util.ServiceLoader;

public class YamlScenarioLoader {
    public Scenario load(final String scenarioPath) {
        return ServiceLoader
            .load(FileScenarioLoader.class)
            .stream()
            .map(ServiceLoader.Provider::get)
            .filter(loader -> loader.getSupportedExtensions().contains("yaml"))
            .findFirst()
            .orElseThrow()
            .load(Path.of(scenarioPath));
    }
}
