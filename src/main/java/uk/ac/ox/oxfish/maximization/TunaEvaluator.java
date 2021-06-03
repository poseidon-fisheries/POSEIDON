package uk.ac.ox.oxfish.maximization;

import com.google.common.primitives.ImmutableDoubleArray;
import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fishing.PurseSeinerFishingStrategyFactory;
import uk.ac.ox.oxfish.maximization.generic.FixedDataTarget;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.model.scenario.TunaScenario;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.google.common.collect.Streams.findLast;
import static java.lang.Runtime.getRuntime;
import static java.util.Arrays.stream;
import static java.util.stream.IntStream.rangeClosed;
import static org.apache.commons.lang3.StringUtils.substringBetween;

public class TunaEvaluator implements Runnable {

    private static final Path DEFAULT_CALIBRATION_FOLDER = Paths.get(
        System.getProperty("user.home"), "tuna", "calibration", "cenv0729", "2021-06-01_17.11.54"
    );

    private final Path calibrationFilePath;
    private final double[] solution;
    private int numRuns = getRuntime().availableProcessors();
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Optional<Consumer<Scenario>> scenarioConsumer = Optional.empty();

    TunaEvaluator(final Path calibrationFilePath, final double[] solution) {
        this.calibrationFilePath = calibrationFilePath;
        this.solution = solution.clone();
    }

    @SuppressWarnings("UnstableApiUsage")
    public static void main(final String[] args) {

        // Finds the first argument that is a folder name and uses it as the calibration folder
        final Path calibrationFolder = getCalibrationFolder(args);

        System.out.println("Using " + calibrationFolder + " as the calibration folder.");

        final Path logFilePath = calibrationFolder.resolve("calibration_log.md");
        final Path calibrationFilePath = calibrationFolder.resolve("calibration.yaml");

        final ImmutableDoubleArray.Builder solutionBuilder = ImmutableDoubleArray.builder();
        try (final Stream<String> lines = Files.lines(logFilePath)) {
            findLast(lines).ifPresent(lastLine -> {
                final String solutionString = substringBetween(lastLine, "{", "}").trim();
                try (final Scanner scanner = new Scanner(solutionString).useDelimiter(", ?")) {
                    while (scanner.hasNextDouble()) solutionBuilder.add(scanner.nextDouble());
                }
            });
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }

        final double[] solution = solutionBuilder.build().toArray();
        final Consumer<Scenario> scenarioConsumer = scenario -> {
            final TunaScenario tunaScenario = (TunaScenario) scenario;
            final PurseSeinerFishingStrategyFactory fishingStrategy = (PurseSeinerFishingStrategyFactory) tunaScenario.getFisherDefinition().getFishingStrategy();
//            fishingStrategy.setFadSetActionLogisticMidpoint(1E6);
//            ((PurseSeineGearFactory) tunaScenario
//                .getFisherDefinition()
//                .getGear())
//                .getFadInitializerFactory().setAttractionRates(ImmutableMap.of(
//                "Bigeye tuna", new FixedDoubleParameter(0.03),
//                "Yellowfin tuna", new FixedDoubleParameter(0.07),
//                "Skipjack tuna", new FixedDoubleParameter(0.1)
//            ));
        };
        new TunaEvaluator(calibrationFilePath, solution)
  //          .setScenarioConsumer(scenarioConsumer)
            .run();

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

        final Path csvOutputFilePath = calibrationFilePath.getParent().resolve("evaluation_results.csv");
        final CsvWriter csvWriter = new CsvWriter(csvOutputFilePath.toFile(), new CsvWriterSettings());

        try {
            final GenericOptimization optimization = GenericOptimization.fromFile(calibrationFilePath);
            csvWriter.writeHeaders(
                "target_class",
                "target_name",
                "target_value",
                "run_number",
                "output_value",
                "error"
            );
            rangeClosed(1, numRuns).parallel().forEach(runNumber -> {
                final FishState fishState = runSimulation(optimization, solution, runNumber, numRuns);
                optimization.getTargets().stream()
                    .filter(target -> target instanceof FixedDataTarget)
                    .map(target -> (FixedDataTarget) target)
                    .forEach(target -> {
                        synchronized (csvWriter) {
                            csvWriter.writeRow(
                                target.getClass().getSimpleName(),
                                target.getColumnName(),
                                target.getFixedTarget(),
                                runNumber,
                                target.getValue(fishState),
                                target.computeError(fishState)
                            );
                        }
                    });
            });
        } finally {
            csvWriter.close();
        }
    }

    private FishState runSimulation(
        final GenericOptimization optimization,
        final double[] optimalParameters,
        final int runNumber,
        final int numRuns
    ) {
        final FishState fishState = new FishState(System.currentTimeMillis());
        final Scenario scenario = makeScenario(optimization, optimalParameters);
        scenarioConsumer.ifPresent(consumer -> consumer.accept(scenario));
        saveEvaluatedScenario(scenario);
        fishState.setScenario(scenario);
        fishState.start();

        do {
            fishState.schedule.step(fishState);
            System.out.printf(
                "---\nRun %3d / %3d, step %5d (year %2d / %2d, day %3d)\n",
                runNumber,
                numRuns,
                fishState.getStep(),
                fishState.getYear() + 1,
                optimization.getSimulatedYears(),
                fishState.getDayOfTheYear()
            );
        } while (fishState.getYear() < optimization.getSimulatedYears());
        return fishState;
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

    private void saveEvaluatedScenario(final Scenario scenario) {
        final Path evaluatedScenarioPath = calibrationFilePath.getParent().resolve("evaluated_scenario.yaml");
        try (final FileWriter fileWriter = new FileWriter(evaluatedScenarioPath.toFile())) {
            new FishYAML().dump(scenario, fileWriter);
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @SuppressWarnings("WeakerAccess")
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
