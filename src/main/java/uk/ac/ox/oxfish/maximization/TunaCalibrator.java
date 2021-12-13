package uk.ac.ox.oxfish.maximization;

import static java.lang.Integer.parseInt;
import static java.lang.Runtime.getRuntime;
import static java.nio.file.Files.createDirectories;
import static java.util.Arrays.stream;

import com.google.common.primitives.ImmutableIntArray;
import eva2.OptimizerFactory;
import eva2.OptimizerRunnable;
import eva2.optimization.OptimizationParameters;
import eva2.optimization.operator.terminators.EvaluationTerminator;
import eva2.optimization.statistics.InterfaceStatisticsParameters;
import eva2.optimization.strategies.ClusterBasedNichingEA;
import eva2.problems.SimpleProblemWrapper;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.stream.IntStream;
import org.jetbrains.annotations.NotNull;
import uk.ac.ox.oxfish.maximization.generic.AbstractLastStepFixedDataTarget;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

@SuppressWarnings("UnstableApiUsage")
public class TunaCalibrator implements Runnable {

    private static final String CALIBRATION_LOG_FILE_NAME = "calibration_log.md";
    private static final String CALIBRATED_SCENARIO_FILE_NAME = "calibrated_scenario.yaml";

    private Path originalCalibrationFilePath =
        Paths
            .get(
                System.getProperty("user.home"),
                "workspace", "tuna", "abundance", "calibration", "outputs"
            )
            .resolve("calibration.yaml");

    private boolean verbose = false;
    private int populationSize = 10;
    private int maxFitnessCalls = 100;

    public static void main(final String[] args) {

        final TunaCalibrator tunaCalibrator = new TunaCalibrator();

        // Parse all given numeric arguments and use the max one to set the number of fitness
        // calls and the min one to set the population size. If there is only one, it's
        // assumed to be the number of fitness calls.
        final ImmutableIntArray numericArgs = getNumericArgs(args);
        if (!numericArgs.isEmpty()) {
            numericArgs.stream().max().ifPresent(tunaCalibrator::setMaxFitnessCalls);
            if (numericArgs.length() > 1) {
                numericArgs.stream().min().ifPresent(tunaCalibrator::setPopulationSize);
            }
        }

        // Finds the first argument that is a file name and uses it as the calibration file
        stream(args)
            .map(File::new)
            .filter(File::isFile)
            .findFirst()
            .ifPresent(file -> tunaCalibrator.setOriginalCalibrationFilePath(file.toPath()));

        tunaCalibrator.run();
    }

    private static ImmutableIntArray getNumericArgs(final String[] args) {
        return ImmutableIntArray.copyOf(stream(args).flatMapToInt(arg -> {
            try {
                return IntStream.of(parseInt(arg));
            } catch (final NumberFormatException e) {
                return IntStream.empty();
            }
        }));
    }

    @Override
    public void run() {
        final Path calibrationFilePath =
            copyToFolder(this.originalCalibrationFilePath, makeOutputFolder());
        final double[] solution = calibrate(calibrationFilePath);
        saveCalibratedScenario(solution, calibrationFilePath);
        new TunaEvaluator(calibrationFilePath, solution).run();
    }

    private static Path copyToFolder(final Path sourceFile, final Path targetFolder) {
        try {
            return Files.copy(sourceFile, targetFolder.resolve(sourceFile.getFileName()));
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @NotNull
    private Path makeOutputFolder() {
        final String outputFolderName =
            new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date());
        final Path outputFolderPath = originalCalibrationFilePath
            .getParent()
            .resolve(System.getProperty("user.name"))
            .resolve(outputFolderName);
        try {
            createDirectories(outputFolderPath);
            final String hostName = InetAddress.getLocalHost().getHostName() + "\n";
            Files.write(outputFolderPath.resolve("hostname.txt"), hostName.getBytes());
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
        return outputFolderPath;
    }

    private double[] calibrate(final Path calibrationFilePath) {

        final Path logFilePath = calibrationFilePath.getParent().resolve(CALIBRATION_LOG_FILE_NAME);
        final GenericOptimization optimizationProblem =
            GenericOptimization.fromFile(calibrationFilePath);

        System.out.println("Running calibration for: " + calibrationFilePath);
        System.out.println("Logging to: " + logFilePath);
        System.out.println("Population size: " + populationSize);
        System.out.println("Max fitness calls: " + maxFitnessCalls);

        optimizationProblem.getTargets().stream()
            .filter(target -> target instanceof AbstractLastStepFixedDataTarget)
            .forEach(target -> ((AbstractLastStepFixedDataTarget) target).setVerbose(verbose));

        final int numThreads = Math.min(getRuntime().availableProcessors(), 32);

        System.out.println("Requesting " + numThreads + " threads");

        final SimpleProblemWrapper problemWrapper = new SimpleProblemWrapper();
        problemWrapper.setSimpleProblem(optimizationProblem);
        problemWrapper.setParallelThreads(numThreads);

        final ClusterBasedNichingEA optimizer = new ClusterBasedNichingEA();
        optimizer.setPopulationSize(populationSize);

        final OptimizationParameters optimizationParameters =
            OptimizerFactory.makeParams(
                optimizer,
                populationSize,
                problemWrapper,
                System.currentTimeMillis(),
                new EvaluationTerminator(maxFitnessCalls)
            );

        final OptimizerRunnable runnable = new OptimizerRunnable(optimizationParameters, "");
        runnable.setOutputFullStatsToText(true);
        runnable.setVerbosityLevel(InterfaceStatisticsParameters.OutputVerbosity.ALL);
        runnable.setOutputTo(InterfaceStatisticsParameters.OutputTo.WINDOW);

        try (
            final FileAndScreenWriter fileAndScreenWriter = new FileAndScreenWriter(logFilePath)
        ) {
            runnable.setTextListener(fileAndScreenWriter);
            runnable.run();
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }

        return runnable.getDoubleSolution();

    }

    private static void saveCalibratedScenario(
        final double[] optimalParameters,
        final Path calibrationFilePath
    ) {

        final Path calibratedScenarioPath =
            calibrationFilePath.getParent().resolve(CALIBRATED_SCENARIO_FILE_NAME);

        try (final FileWriter fileWriter = new FileWriter(calibratedScenarioPath.toFile())) {
            final GenericOptimization optimization =
                GenericOptimization.fromFile(calibrationFilePath);
            final Scenario scenario = GenericOptimization.buildScenario(
                optimalParameters,
                Paths.get(optimization.getScenarioFile()).toFile(),
                optimization.getParameters()
            );
            new FishYAML().dump(scenario, fileWriter);
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @SuppressWarnings("unused")
    public boolean isVerbose() {
        return verbose;
    }

    @SuppressWarnings("unused")
    public void setVerbose(final boolean verbose) {
        this.verbose = verbose;
    }

    @SuppressWarnings("unused")
    public Path getOriginalCalibrationFilePath() {
        return originalCalibrationFilePath;
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    public void setOriginalCalibrationFilePath(final Path originalCalibrationFilePath) {
        this.originalCalibrationFilePath = originalCalibrationFilePath;
    }

    @SuppressWarnings("unused")
    public int getPopulationSize() {
        return populationSize;
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    public void setPopulationSize(final int populationSize) {
        this.populationSize = populationSize;
    }

    @SuppressWarnings("unused")
    public int getMaxFitnessCalls() {
        return maxFitnessCalls;
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    public void setMaxFitnessCalls(final int maxFitnessCalls) {
        this.maxFitnessCalls = maxFitnessCalls;
    }
}
