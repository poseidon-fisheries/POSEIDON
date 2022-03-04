package uk.ac.ox.oxfish.maximization;

import static java.lang.Integer.parseInt;
import static java.lang.Runtime.getRuntime;
import static java.nio.file.Files.createDirectories;
import static java.util.Arrays.stream;
import static uk.ac.ox.oxfish.utility.CsvLogger.addCsvLogger;

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
import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.message.ObjectArrayMessage;
import org.jetbrains.annotations.NotNull;
import sim.engine.SimState;
import uk.ac.ox.oxfish.maximization.generic.AbstractLastStepFixedDataTarget;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

@SuppressWarnings("UnstableApiUsage")
public class TunaCalibrator implements Runnable {

    private static final String CALIBRATION_LOG_FILE_NAME = "calibration_log.md";
    private static final String CALIBRATED_SCENARIO_FILE_NAME = "calibrated_scenario.yaml";
    public static final int MAX_PROCESSORS_TO_USE = 16;

    public static final int DEFAULT_POPULATION_SIZE = 20;
    public final static int MAX_FITNESS_CALLS = 5000;
    public static final int DEFAULT_RANGE = 10;

    private boolean localSearch = false;
    private String runNickName = "global_calibration";
    private int populationSize = DEFAULT_POPULATION_SIZE;
    private int maxFitnessCalls = MAX_FITNESS_CALLS;
    private int parameterRange=  DEFAULT_RANGE;
    private int maxProcessorsToUse = MAX_PROCESSORS_TO_USE;
    /**
     * if this is positive then ignore the number of runs per setting in the original YAML and use this number instead
     */
    private int numberOfRunsPerSettingOverride = -1;



    private Path originalCalibrationFilePath =
            Paths
                    .get(
                            System.getProperty("user.home"),
                            "workspace", "tuna", "calibration", "results"
                    )
                    .resolve("calibration.yaml");

    private boolean verbose = false;


    /**
     * list of individuals we want to force in the original population; usually these are just the output of some previous optimizations
     */
    private List<double[]> bestGuess  = new LinkedList<>();



    public static void main(final String[] args) {

        final TunaCalibrator tunaCalibrator = new TunaCalibrator();
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

    @Override
    public void run() {
        final Path calibrationFilePath =
                copyToFolder(this.originalCalibrationFilePath, makeOutputFolder());
        final double[] solution = calibrate(calibrationFilePath);
        evaluateSolutionAndPrintOutErrors(calibrationFilePath, solution);
    }

    public static void evaluateSolutionAndPrintOutErrors(Path calibrationFilePath, double[] solution) {
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
        String outputFolderName =
                new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date());
        if(runNickName != null && !runNickName.trim().isEmpty())
            outputFolderName= outputFolderName + "_" + runNickName;
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

        if(numberOfRunsPerSettingOverride>0)
            optimizationProblem.setRunsPerSetting(numberOfRunsPerSettingOverride);

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
        if(bestGuess == null)
            problemWrapper = new SimpleProblemWrapper();
        else
            problemWrapper = new SimpleProblemWrapper(){
                @Override
                public void initializePopulation(Population population) {

                    super.initializePopulation(population);

                    for (int scenario = 0; scenario < bestGuess.size(); scenario++) {
                        final ESIndividualDoubleData individual = new ESIndividualDoubleData(
                                (ESIndividualDoubleData) population.get(0));
                        individual.setDoubleGenotype(bestGuess.get(scenario));
                        individual.setDoublePhenotype(bestGuess.get(scenario));

                        population.replaceIndividualAt(scenario,
                                individual);
                    }

                }



            };

        problemWrapper.setSimpleProblem(optimizationProblem);
        problemWrapper.setParallelThreads(numThreads);

        final AbstractOptimizer optimizer;
        if(localSearch){
            optimizer = new NelderMeadSimplex();
            ((NelderMeadSimplex) optimizer).setPopulationSize(populationSize);
        }
        else {
            optimizer = new ClusterBasedNichingEA();
            ((ClusterBasedNichingEA) optimizer).setPopulationSize(populationSize);
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

    public String getRunNickName() {
        return runNickName;
    }

    public void setRunNickName(String runNickName) {
        this.runNickName = runNickName;
    }

    public int getParameterRange() {
        return parameterRange;
    }

    public void setParameterRange(int parameterRange) {
        this.parameterRange = parameterRange;
    }

    public int getMaxProcessorsToUse() {
        return maxProcessorsToUse;
    }

    public void setMaxProcessorsToUse(int maxProcessorsToUse) {
        this.maxProcessorsToUse = maxProcessorsToUse;
    }

    public boolean isLocalSearch() {
        return localSearch;
    }

    public void setLocalSearch(boolean localSearch) {
        this.localSearch = localSearch;
    }

    public List<double[]> getBestGuess() {
        return bestGuess;
    }

    public void setBestGuess(List<double[]> bestGuess) {
        this.bestGuess = bestGuess;
    }

    public int getNumberOfRunsPerSettingOverride() {
        return numberOfRunsPerSettingOverride;
    }

    public void setNumberOfRunsPerSettingOverride(int numberOfRunsPerSettingOverride) {
        this.numberOfRunsPerSettingOverride = numberOfRunsPerSettingOverride;
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

}
