package uk.ac.ox.poseidon.simulations.adaptors;

import com.google.common.collect.ImmutableSet;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;
import uk.ac.ox.poseidon.simulations.api.FileScenarioLoader;
import uk.ac.ox.poseidon.simulations.api.Scenario;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;

public class YamlScenarioLoader implements FileScenarioLoader {
    private static final Set<String> SUPPORTED_EXTENSIONS = ImmutableSet.of("yaml");

    @Override
    public Scenario load(final Path scenarioPath) {
        try (final FileInputStream fileInputStream = new FileInputStream(scenarioPath.toFile())) {
            final InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, UTF_8);
            final FishYAML fishYAML = new FishYAML();
            return new ScenarioAdaptor(
                fishYAML.loadAs(inputStreamReader, uk.ac.ox.oxfish.model.scenario.Scenario.class)
            );
        } catch (final FileNotFoundException e) {
            throw new IllegalArgumentException("Can't find scenario file: " + scenarioPath, e);
        } catch (final IOException e) {
            throw new IllegalStateException("Error while reading file: " + scenarioPath, e);
        }
    }

    @Override
    public Set<String> getSupportedExtensions() {
        return SUPPORTED_EXTENSIONS;
    }
}