package uk.ac.ox.oxfish.experiments.petersnapper;

import com.google.common.collect.Streams;
import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.FlexibleScenario;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

public class DefaultRun {

    private static final Path SCENARIO_PATH = Paths.get("temp", "peter_snapper", "scenario.yaml");

    @SuppressWarnings("UnstableApiUsage")
    public static void main(final String[] args) {
        final FlexibleScenario scenario = loadScenario(SCENARIO_PATH, FlexibleScenario.class);
        final LinkedHashMap<String, Integer> initialFishersPerPort = scenario.getFisherDefinitions().get(0).getInitialFishersPerPort();
        initialFishersPerPort.put("Benoa", 1);
        initialFishersPerPort.put("Kupang", 0);
        final FishState state = new FishState(0);
        state.setScenario(scenario);
        state.start();
        while (state.getYear() < 10)
            state.schedule.step(state);

        final File outputFile = Paths.get("temp", "peter_snapper", "landings.csv").toFile();
        try (final Writer fileWriter = new FileWriter(outputFile, false)) {
            final CsvWriter csvWriter = new CsvWriter(new BufferedWriter(fileWriter), new CsvWriterSettings());
            csvWriter.writeHeaders(List.of("year", "landings"));
            csvWriter.writeRows(
                    Streams.mapWithIndex(
                            state.getYearlyDataSet().getColumn("Peter Snapper Landings").stream(),
                            (landings, year) -> new Object[]{year + 2000, landings}
                    ).collect(Collectors.toList())
            );
            csvWriter.close();
        } catch (final IOException e) {
            throw new IllegalStateException("Writing to " + outputFile + " failed.", e);
        }
    }

    private static <T extends Scenario> T loadScenario(final Path scenarioPath, final Class<T> scenarioClass) {
        try (final FileReader fileReader = new FileReader(scenarioPath.toFile())) {
            final FishYAML fishYAML = new FishYAML();
            return fishYAML.loadAs(fileReader, scenarioClass);
        } catch (final FileNotFoundException e) {
            throw new IllegalArgumentException("Can't find scenario file: " + SCENARIO_PATH, e);
        } catch (final IOException e) {
            throw new IllegalStateException("Error while reading file: " + SCENARIO_PATH, e);
        }
    }
}
