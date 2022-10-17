package uk.ac.ox.oxfish.maximization;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.primitives.ImmutableDoubleArray;
import org.jetbrains.annotations.NotNull;
import uk.ac.ox.oxfish.biology.SpeciesCodes;
import uk.ac.ox.oxfish.experiments.tuna.Policy;
import uk.ac.ox.oxfish.experiments.tuna.Runner;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.AbundanceFadAttractionEvent;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.monitors.loggers.AbundanceFadAttractionEventObserver;
import uk.ac.ox.oxfish.model.data.monitors.loggers.GlobalBiomassLogger;
import uk.ac.ox.oxfish.model.data.monitors.loggers.PurseSeineActionsLogger;
import uk.ac.ox.oxfish.model.data.monitors.loggers.PurseSeineTripLogger;
import uk.ac.ox.oxfish.model.scenario.EpoScenario;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.Streams.findLast;
import static com.google.common.io.Files.getFileExtension;
import static java.lang.Runtime.getRuntime;
import static java.util.Arrays.stream;
import static org.apache.commons.lang3.StringUtils.substringBetween;

public class TunaEvaluator implements Runnable {

    // I identified the vessels to follow using (in R):
    // obs_action_events |>
    //  filter(year == 2017, action_type %in% c("DEL", "FAD", "NOA")) |>
    //  count(ves_no, action_type) |>
    //  group_by(action_type) |>
    //  slice_max(n, with_ties = FALSE)
    private static final Set<String> boatsToTrack = ImmutableSet.of("1779", "453", "1552");

    private static final Path DEFAULT_CALIBRATION_FOLDER = Paths.get(
        System.getProperty("user.home"),
        "workspace", "tuna", "calibration", "results",
        "cenv0729", "2022-09-11_08.27.13_global_calibration"
    );
    private final GenericOptimization optimization;
    private final Runner<Scenario> runner;

    private int numRuns = getRuntime().availableProcessors() + 1;
    private int numYearsToRuns = 3;
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Optional<Consumer<Scenario>> scenarioConsumer = Optional.empty();


    private boolean parallel = false;

    private boolean completeOutput = true;

    public TunaEvaluator(final Path calibrationFilePath, final double[] solution) {

        optimization = GenericOptimization.fromFile(calibrationFilePath);

        runner = new Runner<>(
            () -> makeScenario(optimization, solution),
            calibrationFilePath.getParent()
        ).registerRowProvider(
            "yearly_results.csv",
            fishState -> new YearlyResultsRowProvider(fishState, optimization)
        );
        runner.setParallel(true);

    }

    public TunaEvaluator(final Path alreadyCalibratedScenario, final Path calibrationFilePath) {

        optimization = GenericOptimization.fromFile(calibrationFilePath);

        runner = new Runner<Scenario>(
            () -> {
                try {
                    FishYAML yaml = new FishYAML();
                    return yaml.loadAs(new FileReader(alreadyCalibratedScenario.toFile()), EpoScenario.class);
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
            },
            alreadyCalibratedScenario.getParent()
        ).registerRowProvider(
            "yearly_results.csv",
            fishState -> new YearlyResultsRowProvider(fishState, optimization)
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

    public static void main(final String[] args) {

        // Finds the first argument that is a folder name and uses it as the calibration folder
        final Path calibrationFolder = getCalibrationFolder(args);

        System.out.println("Using " + calibrationFolder + " as the calibration folder.");

        final Path logFilePath = calibrationFolder.resolve("calibration_log.md");
        final Path calibrationFilePath = findCalibrationFile(calibrationFolder);

        final double[] solution = extractSolution(logFilePath);
        final TunaEvaluator tunaEvaluator = new TunaEvaluator(calibrationFilePath, solution);
        tunaEvaluator.setParallel(true);
        tunaEvaluator.run();
    }

    @SuppressWarnings("UnstableApiUsage")
    private static Path findCalibrationFile(Path folder) {
        try (Stream<Path> paths = Files.list(folder)) {
            final ImmutableList<Path> calibrationFiles = paths
                .filter(path -> getFileExtension(path.toString()).equals("yaml"))
                .filter(TunaEvaluator::isCalibrationFile)
                .collect(toImmutableList());
            checkState(!calibrationFiles.isEmpty(), "No calibration files found in %s", folder);
            checkState(calibrationFiles.size() == 1, "More than one calibration files found in %s", folder);
            return calibrationFiles.get(0);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static boolean isCalibrationFile(Path path) {
        try {
            return Files.lines(path)
                .findFirst()
                .filter(line -> line.equals("!!uk.ac.ox.oxfish.maximization.GenericOptimization"))
                .isPresent();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    @NotNull
    private static double[] extractSolution(Path logFilePath) {
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
        return solutionBuilder.build().toArray();
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

    @SuppressWarnings("unused")
    public int getNumYearsToRuns() {
        return numYearsToRuns;
    }

    @SuppressWarnings("unused")
    public void setNumYearsToRuns(int numYearsToRuns) {
        this.numYearsToRuns = numYearsToRuns;
    }

    private void registerFadAttractionEventProviders() {
        runner.setAfterStartConsumer(state -> {
            final FishState fishState = state.getModel();
            final EpoScenario<?, ?> scenario = (EpoScenario<?, ?>) state.getScenario();
            final SpeciesCodes speciesCodes = scenario.grabSpeciesCodesFactory().get();
            final AbundanceFadAttractionEventObserver observer =
                new AbundanceFadAttractionEventObserver(fishState, speciesCodes);
            fishState.getFishers().stream()
                .filter(fisher -> boatsToTrack.contains(fisher.getTags().get(0)))
                .map(FadManager::getFadManager)
                .forEach(fadManager ->
                    fadManager.registerObserver(AbundanceFadAttractionEvent.class, observer)
                );
            if(completeOutput) {
                ImmutableMap.of(
                        "fad_attraction_events.csv", observer.getEventLogger(),
                        "tile_abundance_before.csv", observer.getTileAbundanceLogger(),
                        "fad_abundance_delta.csv", observer.getFadAbundanceLogger()
                ).forEach((fileName, logger) -> runner.registerRowProvider(fileName, __ -> logger));
            }
        });
    }

    @Override
    public void run() {

        runner.setParallel(parallel);
        runner.writeScenarioToFile("calibrated_scenario.yaml");

        scenarioConsumer.ifPresent(consumer ->
            runner.setPolicies(ImmutableList.of(
                new Policy<>("Modified scenario", "", consumer)
            ))
        );

        final AtomicInteger runCounter = new AtomicInteger(1);
        runner.run(numYearsToRuns, numRuns - 1, runCounter);
        runner.registerRowProvider("sim_action_events.csv", PurseSeineActionsLogger::new);
        runner.registerRowProvider("sim_trip_events.csv", PurseSeineTripLogger::new);

        if (!boatsToTrack.isEmpty()) {
            registerFadAttractionEventProviders();
        }

        runner.registerRowProvider("sim_global_biomass.csv", GlobalBiomassLogger::new);
        runner.run(numYearsToRuns, 1, runCounter);

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


    public boolean isParallel() {
        return parallel;
    }

    public void setParallel(boolean parallel) {
        this.parallel = parallel;
    }

    public boolean isCompleteOutput() {
        return completeOutput;
    }

    public void setCompleteOutput(boolean completeOutput) {
        this.completeOutput = completeOutput;
    }
}
