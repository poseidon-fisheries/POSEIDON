package uk.ac.ox.oxfish.maximization;

import com.google.common.collect.ImmutableList;
import com.google.common.primitives.ImmutableIntArray;
import eva2.OptimizerFactory;
import eva2.OptimizerRunnable;
import eva2.optimization.OptimizationParameters;
import eva2.optimization.individuals.ESIndividualDoubleData;
import eva2.optimization.operator.terminators.EvaluationTerminator;
import eva2.optimization.population.Population;
import eva2.optimization.statistics.InterfaceStatisticsParameters;
import eva2.optimization.strategies.AbstractOptimizer;
import eva2.optimization.strategies.ClusterBasedNichingEA;
import eva2.optimization.strategies.NelderMeadSimplex;
import eva2.optimization.strategies.ParticleSwarmOptimizationGCPSO;
import eva2.problems.SimpleProblemWrapper;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.message.ObjectArrayMessage;
import org.jetbrains.annotations.NotNull;
import sim.engine.SimState;
import uk.ac.ox.oxfish.maximization.generic.AbstractLastStepFixedDataTarget;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;

import static java.lang.Integer.parseInt;
import static java.lang.Runtime.getRuntime;
import static java.nio.file.Files.createDirectories;
import static java.util.Arrays.stream;
import static uk.ac.ox.oxfish.utility.CsvLogger.addCsvLogger;

@SuppressWarnings("UnstableApiUsage")
public class TunaCalibrator {

    static final int MAX_PROCESSORS_TO_USE = getRuntime().availableProcessors();
    static final int DEFAULT_POPULATION_SIZE = 100;
    static final int MAX_FITNESS_CALLS = 2000;
    static final int DEFAULT_RANGE = 10;

    private static final String CALIBRATION_LOG_FILE_NAME = "calibration_log.md";
    private static final String CALIBRATED_SCENARIO_FILE_NAME = "calibrated_scenario.yaml";

    private OptimizationRoutine optimizationRoutine = OptimizationRoutine.CLUSTER_NICHING_GA;
    private String runNickName = "global_calibration";
    private int populationSize = DEFAULT_POPULATION_SIZE;
    private int maxFitnessCalls = MAX_FITNESS_CALLS;
    private int parameterRange = DEFAULT_RANGE;
    private int maxProcessorsToUse = MAX_PROCESSORS_TO_USE;
    /**
     * if this is positive then ignore the number of runs per setting in the original YAML and use
     * this number instead
     */
    private int numberOfRunsPerSettingOverride = -1;

    private Path originalCalibrationFilePath = Paths
        .get(
            System.getProperty("user.home"),
            "workspace", "tuna", "calibration", "results"
        )
        .resolve("logistic_calibration.yaml");

    private boolean verbose = false;


    /**
     * list of individuals we want to force in the original population; usually these are just the
     * output of some previous optimizations
     */
    private List<double[]> bestGuess = new LinkedList<>();


    public static void main(final String[] args) {


        final TunaCalibrator tunaCalibrator = new TunaCalibrator();

        tunaCalibrator.setBestGuess(ImmutableList.of(
            new double[]{0.313, -5.761, 0.809, -3.742, -10.000, -7.861, 2.365, -2.139, 8.461, 5.007, 4.444, -1.546, -3.790, 4.470, 8.133, -10.000, 5.963, 7.210, 6.646, 0.393, -5.293, 1.765, -1.370, -7.434, 0.300, -9.658, 4.065, -9.686, 3.105, -0.150, -8.127, 9.834, -10.000, -9.417, -5.385, -8.168, 10.000, -3.846, -1.639, 3.979, 5.605, -2.995, 6.011, 4.926, 8.673, 1.998, -9.186, -6.695, 0.550, 0.123, 2.675, -4.567, 8.381, -8.140, 8.402, 7.539, 7.410},
            new double[]{3.782, -0.632, 6.757, 3.293, -0.651, 4.139, -4.293, 10.000, -0.703, 5.301, -3.247, -6.799, -2.802, 4.687, 3.858, -7.355, 8.727, 7.075, -4.786, 4.701, 0.902, 2.674, -5.527, 0.944, -3.984, -9.301, 0.959, 9.570, -1.391, -3.476, -8.894, -2.100, 9.674, 7.360, -3.604, 1.303, -5.967, -0.382, -3.650, -10.000, -2.916, -10.000, 1.362, 2.151, -3.573, -2.072, -1.465, 4.509, -0.078, 7.464, 1.187, 1.241, 0.911, 2.298, -2.489, 3.550, -0.986},
            new double[]{3.929, 1.729, 2.601, 1.338, -0.938, 8.053, 2.388, 8.373, 1.670, 6.009, -7.618, -9.670, -6.031, 4.673, -2.834, -1.093, 10.000, 8.524, -4.045, 8.547, -4.509, 4.278, 1.113, 1.181, -7.505, -9.556, 5.384, 9.684, 8.217, -3.704, -8.807, -4.014, 10.000, 6.842, -0.635, -1.195, -7.965, 0.456, -7.540, -7.308, -5.415, -10.000, 4.988, 4.732, 4.192, 0.704, -4.109, 5.095, 0.391, 6.507, -5.303, -0.476, 5.861, -2.811, 2.487, 0.706, 1.179}
        ));

        addCsvLogger(
            Level.DEBUG,
            "calibration_error",
            "time,error"
        );
        addCsvLogger(
            Level.DEBUG,
            "run_timer",
            "thread,run,step,time"
        );

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

    static void evaluateSolutionAndPrintOutErrors(
        final Path calibrationFilePath,
        final double[] solution
    ) {
        saveCalibratedScenario(solution, calibrationFilePath);
        new TunaEvaluator(calibrationFilePath, solution).run();
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

    private static Path copyToFolder(final Path sourceFile, final Path targetFolder) {
        try {
            return Files.copy(sourceFile, targetFolder.resolve(sourceFile.getFileName()));
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static void logCurrentTime(final SimState simState) {
        LogManager.getLogger("run_timer").debug(() ->
            new ObjectArrayMessage(
                Thread.currentThread().getId(),
                ((FishState) simState).getTrulyUniqueID(),
                ((FishState) simState).getStep(),
                System.currentTimeMillis()
            )
        );
    }

    public double[] run() {
        final Path calibrationFilePath =
            copyToFolder(this.originalCalibrationFilePath, makeOutputFolder());
        final double[] solution = calibrate(calibrationFilePath);
        evaluateSolutionAndPrintOutErrors(calibrationFilePath, solution);
        return solution;
    }

    @NotNull
    private Path makeOutputFolder() {
        String outputFolderName =
            new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date());
        if (runNickName != null && !runNickName.trim().isEmpty()) {
            outputFolderName = outputFolderName + "_" + runNickName;
        }
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

        if (numberOfRunsPerSettingOverride > 0) {
            optimizationProblem.setRunsPerSetting(numberOfRunsPerSettingOverride);
        }

        System.out.println("Running calibration for: " + calibrationFilePath);
        System.out.println("Logging to: " + logFilePath);
        System.out.println("Population size: " + populationSize);
        System.out.println("Max fitness calls: " + maxFitnessCalls);

        optimizationProblem.getTargets().stream()
            .filter(target -> target instanceof AbstractLastStepFixedDataTarget)
            .forEach(target -> ((AbstractLastStepFixedDataTarget) target).setVerbose(verbose));

        final int numThreads = Math.min(getRuntime().availableProcessors(), maxProcessorsToUse);

        System.out.println("Requesting " + numThreads + " threads");
        final SimpleProblemWrapper problemWrapper;
        if (bestGuess == null) {
            problemWrapper = new SimpleProblemWrapper();
        } else {
            problemWrapper = new SimpleProblemWrapper() {
                @Override
                public void initializePopulation(final Population population) {

                    super.initializePopulation(population);

                    for (int scenario = 0; scenario < bestGuess.size(); scenario++) {
                        final ESIndividualDoubleData individual = new ESIndividualDoubleData(
                            (ESIndividualDoubleData) population.get(0));
                        individual.setDoubleGenotype(bestGuess.get(scenario));
                        individual.setDoublePhenotype(bestGuess.get(scenario));

                        population.replaceIndividualAt(
                            scenario,
                            individual
                        );
                    }

                }


            };
        }

        problemWrapper.setSimpleProblem(optimizationProblem);
        problemWrapper.setParallelThreads(numThreads);

        final AbstractOptimizer optimizer;
        switch (optimizationRoutine) {
            case NELDER_MEAD:
                optimizer = new NelderMeadSimplex();
                ((NelderMeadSimplex) optimizer).setPopulationSize(populationSize);
                break;
            case PARTICLE_SWARM:
                optimizer = new ParticleSwarmOptimizationGCPSO();
                optimizer.setPopulation(new Population(populationSize));
                ((ParticleSwarmOptimizationGCPSO) optimizer).setCheckRange(false);
                ((ParticleSwarmOptimizationGCPSO) optimizer).setGcpso(true);
                break;
            default:
            case CLUSTER_NICHING_GA:
                optimizer = new ClusterBasedNichingEA();
                ((ClusterBasedNichingEA) optimizer).setPopulationSize(populationSize);
                break;
        }
        problemWrapper.setDefaultRange(parameterRange);

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

    @SuppressWarnings("unused")
    public String getRunNickName() {
        return runNickName;
    }

    void setRunNickName(final String runNickName) {
        this.runNickName = runNickName;
    }

    @SuppressWarnings("unused")
    public int getParameterRange() {
        return parameterRange;
    }

    void setParameterRange(final int parameterRange) {
        this.parameterRange = parameterRange;
    }

    @SuppressWarnings("unused")
    public int getMaxProcessorsToUse() {
        return maxProcessorsToUse;
    }

    void setMaxProcessorsToUse(final int maxProcessorsToUse) {
        this.maxProcessorsToUse = maxProcessorsToUse;
    }


    public OptimizationRoutine getOptimizationRoutine() {
        return optimizationRoutine;
    }

    public void setOptimizationRoutine(OptimizationRoutine optimizationRoutine) {
        this.optimizationRoutine = optimizationRoutine;
    }

    List<double[]> getBestGuess() {
        //noinspection AssignmentOrReturnOfFieldWithMutableType
        return bestGuess;
    }

    void setBestGuess(final List<double[]> bestGuess) {
        //noinspection AssignmentOrReturnOfFieldWithMutableType
        this.bestGuess = bestGuess;
    }

    @SuppressWarnings("unused")
    public int getNumberOfRunsPerSettingOverride() {
        return numberOfRunsPerSettingOverride;
    }

    @SuppressWarnings("WeakerAccess")
    public void setNumberOfRunsPerSettingOverride(final int numberOfRunsPerSettingOverride) {
        this.numberOfRunsPerSettingOverride = numberOfRunsPerSettingOverride;
    }

    public static enum OptimizationRoutine {

        NELDER_MEAD,

        PARTICLE_SWARM,

        CLUSTER_NICHING_GA

    }

}
