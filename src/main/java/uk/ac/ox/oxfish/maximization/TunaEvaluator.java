package uk.ac.ox.oxfish.maximization;

import static com.google.common.collect.Streams.findLast;
import static java.lang.Runtime.getRuntime;
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
import java.util.function.Consumer;
import java.util.stream.Stream;
import uk.ac.ox.oxfish.experiments.tuna.Policy;
import uk.ac.ox.oxfish.experiments.tuna.Runner;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fishing.PurseSeinerBiomassFishingStrategyFactory;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.model.scenario.TunaScenario;

public class TunaEvaluator implements Runnable {

    private static final Path DEFAULT_CALIBRATION_FOLDER = Paths.get(
        System.getProperty("user.home"), "tuna", "calibration", "cenv0729", "2021-06-24_16.37.09"
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
                    while (scanner.hasNextDouble()) {
                        solutionBuilder.add(scanner.nextDouble());
                    }
                }
            });
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }

        final double[] solution = solutionBuilder.build().toArray();
        final Consumer<Scenario> scenarioConsumer = scenario -> {
            final TunaScenario tunaScenario = (TunaScenario) scenario;
            final PurseSeinerBiomassFishingStrategyFactory fishingStrategy =
                (PurseSeinerBiomassFishingStrategyFactory) tunaScenario.getFisherDefinition()
                    .getFishingStrategy();
            //fishingStrategy.setFadDeploymentActionLogisticMidpoint(1);
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
            //.setScenarioConsumer(scenarioConsumer)
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

        final GenericOptimization optimization = GenericOptimization.fromFile(calibrationFilePath);

        final Runner<Scenario> runner =
            new Runner<>(
                () -> makeScenario(optimization, solution),
                calibrationFilePath.getParent()
            )
                .registerRowProvider(
                    "evaluation_results.csv",
                    fishState -> new EvaluationResultsRowProvider(fishState, optimization)
                );

        runner.writeScenarioToFile("calibrated_scenario.yaml");

        scenarioConsumer.ifPresent(consumer ->
            runner.setPolicies(ImmutableList.of(
                new Policy<>("Modified scenario", "", consumer)
            ))
        );

        runner.run(optimization.getSimulatedYears(), numRuns);

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
