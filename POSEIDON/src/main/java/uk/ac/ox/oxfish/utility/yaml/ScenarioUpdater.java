package uk.ac.ox.oxfish.utility.yaml;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.EpoScenario;
import uk.ac.ox.oxfish.model.scenario.Scenario;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;

public class ScenarioUpdater {

    public static void updateScenario(
        final Path inputScenario,
        final Path outputScenario,
        final Function<Stream<String>, String> lineProcessor,
        final Consumer<EpoScenario<?, ?>> scenarioConsumer
    ) {
        try (final Stream<String> scenarioLines = Files.lines(inputScenario)) {

            final String scenarioYaml = lineProcessor.apply(scenarioLines);
            System.out.println(scenarioYaml);
            final FishYAML fishYAML = new FishYAML();
            final EpoScenario<?, ?> scenario = fishYAML.loadAs(scenarioYaml, EpoScenario.class);
            scenarioConsumer.accept(scenario);
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