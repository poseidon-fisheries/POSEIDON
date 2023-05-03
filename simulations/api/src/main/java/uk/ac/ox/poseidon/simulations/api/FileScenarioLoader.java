package uk.ac.ox.poseidon.simulations.api;

import java.nio.file.Path;
import java.util.Set;

public interface FileScenarioLoader {
    Scenario load(Path scenarioPath);
    Set<String> getSupportedExtensions();
}
