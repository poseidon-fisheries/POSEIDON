package uk.ac.ox.oxfish.utility.yaml;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.EpoScenario;
import uk.ac.ox.oxfish.model.scenario.Scenario;

import java.io.*;
import java.nio.file.Path;

import static java.nio.charset.StandardCharsets.UTF_8;

public class ScenarioUpdater {

    public static void updateScenario(
        final Path inputScenario,
        final Path outputScenario
    ) {
        try (final FileInputStream fileInputStream = new FileInputStream(inputScenario.toFile())) {
            final InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, UTF_8);
            final FishYAML fishYAML = new FishYAML();
            final EpoScenario<?, ?> scenario = fishYAML.loadAs(inputStreamReader, EpoScenario.class);
            scenario.getCatchSamplersFactory().setSpeciesCodesSupplier(scenario.getSpeciesCodesSupplier());
            try (final FileOutputStream fileOutputStream = new FileOutputStream(outputScenario.toFile())) {
                final OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
                fishYAML.dump(scenario, outputStreamWriter);
            }
        } catch (final FileNotFoundException e) {
            throw new IllegalArgumentException("Can't find scenario file: " + inputScenario, e);
        } catch (final IOException e) {
            throw new IllegalStateException("Error while reading file: " + inputScenario, e);
        }
    }

}
