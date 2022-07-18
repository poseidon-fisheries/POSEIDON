package uk.ac.ox.oxfish.maximization;

import static com.google.common.collect.Streams.findLast;
import static java.util.Arrays.stream;
import static org.apache.commons.lang3.StringUtils.substringBetween;

import com.google.common.collect.ImmutableList;
import com.google.common.primitives.ImmutableDoubleArray;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.apache.logging.log4j.Level;
import uk.ac.ox.oxfish.experiments.tuna.Policy;
import uk.ac.ox.oxfish.experiments.tuna.Runner;
import uk.ac.ox.oxfish.model.data.monitors.loggers.FadBiomassLogger;
import uk.ac.ox.oxfish.model.data.monitors.loggers.GlobalBiomassLogger;
import uk.ac.ox.oxfish.model.data.monitors.loggers.PurseSeineActionsLogger;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.CsvLogger;

public class TunaEvaluator implements Runnable {

    private static final Path DEFAULT_CALIBRATION_FOLDER = Paths.get(
            "docs/20220223 tuna_calibration/pathfinder_julydata/carrknight/2022-07-10_10.13.49_catchability_original/local/carrknight/2022-07-11_07.51.40_catchability_local/test_yearlyreset/calibrated_scenario.yaml"
    );
    private final GenericOptimization optimization;
    private final Runner<Scenario> runner;
    private int numRuns = 5; //getRuntime().availableProcessors();
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Optional<Consumer<Scenario>> scenarioConsumer = Optional.empty();

    public TunaEvaluator(final Path calibrationFilePath, final double[] solution) {

        optimization = GenericOptimization.fromFile(calibrationFilePath);

        runner = new Runner<>(
            () -> makeScenario(optimization, solution),
            calibrationFilePath.getParent()
        ).registerRowProvider(
            "evaluation_results.csv",
            fishState -> new EvaluationResultsRowProvider(fishState, optimization)
        );
        runner.setParallel(false);

    }

    private static Scenario makeScenario(
        final GenericOptimization optimization,
        final double[] optimalParameters
    ) {
        try {
            return GenericOptimization.buildScenario(
                optimalParameters,
                Paths.get(optimization.getScenarioFile()).toFile(),
                optimization.getParameters()
            );
        } catch (final FileNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    public static void main(final String[] args) {

        // Finds the first argument that is a folder name and uses it as the calibration folder
        final Path calibrationFolder = getCalibrationFolder(args);

        System.out.println("Using " + calibrationFolder + " as the calibration folder.");

        final Path logFilePath = calibrationFolder.resolve("calibration_log.md");
        final Path calibrationFilePath = calibrationFolder.resolve("logistic_calibration.yaml");

        final ImmutableDoubleArray.Builder solutionBuilder = ImmutableDoubleArray.builder();
        try (final Stream<String> lines = Files.lines(logFilePath)) {
            findLast(lines).ifPresent(lastLine -> {
                final String solutionString = substringBetween(lastLine, "{", "}").trim();
                try (final Scanner scanner = new Scanner(solutionString).useDelimiter(", ?")) {
                    while (scanner.hasNextDouble()) {
                        solutionBuilder.add(scanner.nextDouble());
                    }
                }
            });
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }

        final double[] solution = solutionBuilder.build().toArray();
        new TunaEvaluator(calibrationFilePath, solution).run();

    }

    private static Path getCalibrationFolder(final String[] args) {
        return stream(args)
            .map(Paths::get)
            .filter(Files::isDirectory)
            .map(path -> {
                try {
                    return path.toRealPath();
                } catch (final IOException e) {
                    throw new IllegalArgumentException(e);
                }
            })
            .findFirst()
            .orElse(DEFAULT_CALIBRATION_FOLDER);
    }

    @Override
    public void run() {

        runner.writeScenarioToFile("calibrated_scenario.yaml");

        scenarioConsumer.ifPresent(consumer ->
            runner.setPolicies(ImmutableList.of(
                new Policy<>("Modified scenario", "", consumer)
            ))
        );

        final AtomicInteger runCounter = new AtomicInteger(1);
        runner.run(optimization.getSimulatedYears(), numRuns - 1, runCounter);
        runner
            .registerRowProvider("actions.csv", PurseSeineActionsLogger::new);
         //   .registerRowProvider("fad_biomass.csv", FadBiomassLogger::new)
      //      .registerRowProvider("global_biomass.csv", GlobalBiomassLogger::new);
        runner.run(optimization.getSimulatedYears(), 1, runCounter);

    }

    public Runner<Scenario> getRunner() {
        return runner;
    }

    @SuppressWarnings({"WeakerAccess", "unused"})
    public TunaEvaluator setScenarioConsumer(final Consumer<Scenario> scenarioConsumer) {
        this.scenarioConsumer = Optional.ofNullable(scenarioConsumer);
        return this;
    }

    @SuppressWarnings("unused")
    public int getNumRuns() {
        return numRuns;
    }

    @SuppressWarnings("unused")
    public void setNumRuns(final int numRuns) {
        this.numRuns = numRuns;
    }

}
