package uk.ac.ox.oxfish.experiments.tuna;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.PathConverter;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A relatively simple "runner" class that just loads a scenario from a YAML file
 * and runs it for a given number of years, without all the complications of the
 * {@link Runner} class.
 */
public class YamlScenarioRunner {

    @Parameter(names = {"-s", "--seed"})
    private long rngSeed = System.currentTimeMillis();
    @Parameter(names = {"-y", "--years"})
    private int numYearsToRun = 2;
    @Parameter(converter = PathConverter.class)
    private Path scenarioPath =
        Paths.get(
            System.getProperty("user.home"),
            "workspace", "tuna",
            "np", "calibrations",
            "2023-09-04", "cenv0729", "2023-09-15_14.48.13_local",
            "calibrated_scenario.yaml"
        );

    public static void main(final String[] args) throws IOException {
        final YamlScenarioRunner yamlScenarioRunner = new YamlScenarioRunner();
        JCommander.newBuilder()
            .addObject(yamlScenarioRunner)
            .build()
            .parse(args);
        yamlScenarioRunner.run();
    }

    private void run() {
        final Scenario scenario = loadScenario();
        final FishState fishState = new FishState(rngSeed);
        fishState.setScenario(scenario);
        fishState.start();
        do {
            fishState.schedule.step(fishState);
        } while (fishState.getYear() < numYearsToRun);
    }

    private Scenario loadScenario() {
        try (final FileReader fileReader = new FileReader(scenarioPath.toFile())) {
            final FishYAML fishYAML = new FishYAML();
            return fishYAML.loadAs(fileReader, Scenario.class);
        } catch (final FileNotFoundException e) {
            throw new IllegalArgumentException("Can't find scenario file: " + scenarioPath, e);
        } catch (final IOException e) {
            throw new IllegalStateException("Error while reading file: " + scenarioPath, e);
        }
    }

    public int getNumYearsToRun() {
        return numYearsToRun;
    }

    public void setNumYearsToRun(final int numYearsToRun) {
        this.numYearsToRun = numYearsToRun;
    }

    public long getRngSeed() {
        return rngSeed;
    }

    public void setRngSeed(final long rngSeed) {
        this.rngSeed = rngSeed;
    }

    public Path getScenarioPath() {
        return scenarioPath;
    }

    public void setScenarioPath(final Path scenarioPath) {
        this.scenarioPath = scenarioPath;
    }
}
