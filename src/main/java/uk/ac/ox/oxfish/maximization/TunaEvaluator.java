package uk.ac.ox.oxfish.maximization;

import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.ImmutableDoubleArray;
import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.PurseSeineGearFactory;
import uk.ac.ox.oxfish.maximization.generic.FixedDataTarget;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.model.scenario.TunaScenario;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
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
import static java.util.stream.IntStream.rangeClosed;
import static org.apache.commons.lang3.StringUtils.substringBetween;

public class TunaEvaluator implements Runnable {

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

        final String calibrationFolderName = "2021-04-14_09.59.25";

        final Path baseFolderPath = Paths.get(
            System.getProperty("user.home"), "workspace", "tuna", "np", "calibrations"
        );

        final Path calibrationFolderPath = baseFolderPath.resolve(calibrationFolderName);
        final Path logFilePath = calibrationFolderPath.resolve("calibration_log.md");
        final Path calibrationFilePath = calibrationFolderPath.resolve("calibration.yaml");

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
        new TunaEvaluator(calibrationFilePath, solution)
//            .setScenarioConsumer(scenario -> {
//                final TunaScenario tunaScenario = (TunaScenario) scenario;
//                ((PurseSeineGearFactory) tunaScenario
//                    .getFisherDefinition()
//                    .getGear())
//                    .getFadInitializerFactory().setAttractionRates(ImmutableMap.of(
//                    "Bigeye tuna", new FixedDoubleParameter(1),
//                    "Yellowfin tuna", new FixedDoubleParameter(1),
//                    "Skipjack tuna", new FixedDoubleParameter(1)
//                ));
//            })
            .run();

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
                fishState.getFadMap().getDriftingObjectsMap().getCurrentVectors().getVectorCache().values().stream()
                    .mapToDouble(cache -> cache.stats().hitRate()).average()
                    .ifPresent(hitRate -> System.out.println("Hit rate: " + hitRate));
                optimization.getTargets().stream()
                    .filter(target -> target instanceof FixedDataTarget)
                    .map(target -> (FixedDataTarget) target)
                    .forEach(target ->
                        csvWriter.writeRow(
                            target.getClass().getSimpleName(),
                            target.getColumnName(),
                            target.getFixedTarget(),
                            runNumber,
                            target.getValue(fishState),
                            target.computeError(fishState)
                        )
                    );
            });
        } finally {
            csvWriter.close();
        }
    }

    @SuppressWarnings("WeakerAccess")
    public TunaEvaluator setScenarioConsumer(final Consumer<Scenario> scenarioConsumer) {
        this.scenarioConsumer = Optional.ofNullable(scenarioConsumer);
        return this;
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


    private void saveEvaluatedScenario(final Scenario scenario) {
        final Path evaluatedScenarioPath = calibrationFilePath.getParent().resolve("evaluated_scenario.yaml");
        try (final FileWriter fileWriter = new FileWriter(evaluatedScenarioPath.toFile())) {
            new FishYAML().dump(scenario, fileWriter);
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
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

    @SuppressWarnings("unused")
    public int getNumRuns() {
        return numRuns;
    }

    @SuppressWarnings("unused")
    public void setNumRuns(final int numRuns) {
        this.numRuns = numRuns;
    }

}
