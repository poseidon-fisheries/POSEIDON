package uk.ac.ox.oxfish.maximization;

import eva2.OptimizerFactory;
import eva2.OptimizerRunnable;
import eva2.optimization.OptimizationParameters;
import eva2.optimization.operator.terminators.EvaluationTerminator;
import eva2.optimization.statistics.InterfaceStatisticsParameters;
import eva2.optimization.strategies.ClusterBasedNichingEA;
import eva2.problems.SimpleProblemWrapper;
import org.jetbrains.annotations.NotNull;
import uk.ac.ox.oxfish.maximization.generic.AbstractLastStepFixedDataTarget;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.stream.IntStream;

import static java.lang.Integer.parseInt;
import static java.lang.Runtime.getRuntime;
import static java.nio.file.Files.createDirectory;
import static java.util.Arrays.stream;

public class TunaCalibrator implements Runnable {

    private static final String CALIBRATION_LOG_FILE_NAME = "calibration_log.md";
    private static final String CALIBRATED_SCENARIO_FILE_NAME = "calibrated_scenario.yaml";

    private Path originalCalibrationFilePath =
        Paths
            .get(System.getProperty("user.home"), "workspace", "tuna", "np", "calibrations")
            .resolve("calibration.yaml");

    private boolean verbose = false;
    private int populationSize = 200;
    private int maxFitnessCalls = 5000;

    public static void main(String[] args) {

        TunaCalibrator tunaCalibrator = new TunaCalibrator();

        // Lots of code for not much, but this parse all integer numbers out from the
        // arguments and (if present) uses the max one to set the number of fitness calls
        // and the min one to set the population size. If there is only one, it's
        // assumed to be the number of fitness calls.
        int[] numericArgs = stream(args).flatMapToInt(arg -> {
            try {
                return IntStream.of(parseInt(arg));
            } catch (NumberFormatException e) {
                return IntStream.empty();
            }
        }).toArray();
        if (numericArgs.length > 0) {
            tunaCalibrator.setMaxFitnessCalls(stream(numericArgs).max().getAsInt());
            if (numericArgs.length > 1) {
                tunaCalibrator.setPopulationSize(stream(numericArgs).min().getAsInt());
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

    @Override
    public void run() {
        final Path calibrationFilePath = copyToFolder(this.originalCalibrationFilePath, makeOutputFolder());
        final double[] solution = calibrate(calibrationFilePath);
        saveCalibratedScenario(solution, calibrationFilePath);
        new TunaEvaluator(calibrationFilePath, solution).run();
    }

    private Path copyToFolder(Path sourceFile, Path targetFolder) {
        try {
            return Files.copy(sourceFile, targetFolder.resolve(sourceFile.getFileName()));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @NotNull
    private Path makeOutputFolder() {
        String outputFolderName = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date());
        Path outputFolderPath = originalCalibrationFilePath.getParent().resolve(outputFolderName);
        try {
            createDirectory(outputFolderPath);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return outputFolderPath;
    }

    private double[] calibrate(final Path calibrationFilePath) {

        final Path logFilePath = calibrationFilePath.getParent().resolve(CALIBRATION_LOG_FILE_NAME);
        final GenericOptimization optimizationProblem = GenericOptimization.fromFile(calibrationFilePath);

        System.out.println("Running calibration for: " + calibrationFilePath);
        System.out.println("Logging to: " + logFilePath);
        System.out.println("Population size: " + populationSize);
        System.out.println("Max fitness calls: " + maxFitnessCalls);

        optimizationProblem.getTargets().stream()
            .filter(target -> target instanceof AbstractLastStepFixedDataTarget)
            .forEach(target -> ((AbstractLastStepFixedDataTarget) target).setVerbose(verbose));

        final int numThreads = Math.min(getRuntime().availableProcessors(), 32);

        System.out.println("Requesting " + numThreads + " threads");

        SimpleProblemWrapper problemWrapper = new SimpleProblemWrapper();
        problemWrapper.setSimpleProblem(optimizationProblem);
        problemWrapper.setParallelThreads(numThreads);

        ClusterBasedNichingEA optimizer = new ClusterBasedNichingEA();
        optimizer.setPopulationSize(populationSize);

        final OptimizationParameters optimizationParameters =
            OptimizerFactory.makeParams(
                optimizer,
                populationSize,
                problemWrapper,
                System.currentTimeMillis(),
                new EvaluationTerminator(maxFitnessCalls)
            );

        OptimizerRunnable runnable = new OptimizerRunnable(optimizationParameters, "");
        runnable.setOutputFullStatsToText(true);
        runnable.setVerbosityLevel(InterfaceStatisticsParameters.OutputVerbosity.ALL);
        runnable.setOutputTo(InterfaceStatisticsParameters.OutputTo.WINDOW);

        try (
            final FileAndScreenWriter fileAndScreenWriter = new FileAndScreenWriter(logFilePath)
        ) {
            runnable.setTextListener(fileAndScreenWriter);
            runnable.run();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        return runnable.getDoubleSolution();

    }

    public void saveCalibratedScenario(double[] optimalParameters, Path calibrationFilePath) {

        final Path calibratedScenarioPath = calibrationFilePath.getParent().resolve(CALIBRATED_SCENARIO_FILE_NAME);
        GenericOptimization.saveCalibratedScenario(optimalParameters,calibrationFilePath,calibratedScenarioPath);
    }

    @SuppressWarnings("unused")
    public boolean isVerbose() {
        return verbose;
    }

    @SuppressWarnings("unused")
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    @SuppressWarnings("unused")
    public Path getOriginalCalibrationFilePath() {
        return originalCalibrationFilePath;
    }

    @SuppressWarnings("unused")
    public void setOriginalCalibrationFilePath(Path originalCalibrationFilePath) {
        this.originalCalibrationFilePath = originalCalibrationFilePath;
    }

    @SuppressWarnings("unused")
    public int getPopulationSize() {
        return populationSize;
    }

    @SuppressWarnings("unused")
    public void setPopulationSize(int populationSize) {
        this.populationSize = populationSize;
    }

    @SuppressWarnings("unused")
    public int getMaxFitnessCalls() {
        return maxFitnessCalls;
    }

    @SuppressWarnings("unused")
    public void setMaxFitnessCalls(int maxFitnessCalls) {
        this.maxFitnessCalls = maxFitnessCalls;
    }
}
