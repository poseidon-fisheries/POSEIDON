package uk.ac.ox.oxfish.maximization;

import static java.lang.Integer.parseInt;
import static java.lang.Runtime.getRuntime;
import static java.nio.file.Files.createDirectories;
import static java.util.Arrays.stream;

import com.google.common.primitives.ImmutableIntArray;
import eva2.OptimizerFactory;
import eva2.OptimizerRunnable;
import eva2.optimization.OptimizationParameters;
import eva2.optimization.individuals.ESIndividualDoubleData;
import eva2.optimization.operator.terminators.EvaluationTerminator;
import eva2.optimization.population.Population;
import eva2.optimization.statistics.InterfaceStatisticsParameters;
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
                            "code", "tuna", "tuna", "calibration", "results", "oneboat_duds"
                    )
                    .resolve("calibration.yaml");

    private boolean verbose = false;
    private int populationSize = 60;
    private int maxFitnessCalls = 10000;


    static private double[][] bestGuess;
//    static {
//        bestGuess = new double[6][];
//
//        bestGuess[0]=
//                new double[]{10.0,-5.6160912064634605,5.023416005159477,-9.306231884786627,6.24783017299497,-10.0,-9.792986811992932,-2.6623480444730476,1.7455584099374928,-10.0,7.946246292621867,4.289373665720819,-7.122212179575932,1.93368576467631,-10.0,10.0,8.559502590302312,0.9487125299037729,-0.8370024899225057,-10.0,-5.592327223568298,-5.228621233954158,-9.121306938816447,7.736607665760178,4.631729656620242,2.8905407411977486,9.185851377113986,10.0,-5.4413134048804315,-10.0,-9.09917715234922,6.806767726034189,0.9018953629363775,7.762252031605188,-2.815912181321279,1.0417013054849251,-10.0,-0.8882152956408718,0.15727357782624418,10.0,-1.1291211941783106,4.977435154957524,6.657886105910654,5.389181089165312,7.274500260614437,7.8923936424872725,1.034666381157146,8.402061122722994,-8.389356579687057,6.5545485646886,1.806624201221273,9.061541914142051,10.0,7.610862541997896,2.587081240312691,-10};
//        bestGuess[5]=
//                new double[]{
//                        4.410, 5.813, 5.498,-15.000, 2.670,-14.216,-14.801, 0.010,-5.543,-7.068, 10.011, 13.368,-4.134,-2.044,-13.676, 3.727,-0.427, 6.816, 1.615,-12.148,-7.022,-4.427,-10.379, 11.996, 15.000, 10.423, 0.327, 8.501,-8.446,-15.000,-10.238, 11.340,-4.411, 7.098, 2.711, 8.396,-13.200, 5.535,-3.622, 15.000,-8.207, 7.995, 13.449, 5.759, 12.215, 1.440, 0.820, 2.556,-4.252, 0.071,-2.993, 13.084, 14.169, 10.976, 1.157,-8};
//
//        bestGuess[1]=new double[]{7.183, 1.028, 0.294,-10.000, 10.000,-9.494,-6.250, 0.550, 5.727,-7.207, 7.731, 3.994,-1.786,-1.942,-10.000, 9.684, 10.000,-1.910, 5.066,-8.582,-6.106,-4.867,-8.814, 10.000, 5.379, 5.812, 10.000, 8.462,-9.296,-10.000,-7.473, 4.113, 2.591, 8.208,-1.169,-0.815,-10.000,-0.236, 1.420, 10.000,-0.386, 6.794, 8.042,-1.170, 7.001, 10.000, 2.907, 4.276,-5.919, 5.482, 0.559, 7.495, 9.317, 6.660, 6.850,-5};
//        bestGuess[2]=new double[]{9.426,-5.614, 4.703,-9.671, 5.107,-9.728,-8.972,-3.143, 0.524,-9.125, 7.708, 4.384,-6.238, 1.763,-10.290, 9.521, 8.245, 0.715,-1.156,-9.889,-5.415,-4.614,-8.714, 6.880, 5.160, 2.295, 8.372, 9.085,-5.578,-9.076,-8.668, 6.564, 1.467, 7.624,-2.565, 0.724,-9.994,-0.603,-0.761, 9.990,-0.487, 4.162, 6.144, 4.448, 6.697, 7.631, 0.490, 7.847,-7.953, 6.689, 1.659, 8.360, 9.566, 6.880, 2.314,-2};
//         bestGuess[3]= new double[]{9.787,-5.705, 4.632,-9.401, 4.865,-10.757,-9.836,-2.797, 1.204,-9.963, 7.732, 4.405,-7.046, 2.129,-10.097, 9.293, 7.710, 0.475,-1.443,-9.427,-5.501,-5.137,-8.184, 6.965, 4.900, 2.251, 9.024, 9.387,-4.637,-9.831,-8.642, 6.812, 0.829, 7.390,-2.933, 1.430,-9.320,-0.729,-0.317, 10.048,-0.538, 4.876, 6.573, 4.879, 7.264, 7.757, 0.450, 7.676,-7.982, 6.428, 1.093, 8.008, 8.990, 7.491, 2.341,0};
//        bestGuess[4]=new double[]{6.864, 3.911, 0.463,-10.000, 9.448,-9.751,-5.904,-1.052, 4.046,-6.625, 7.675, 0.534,-3.932,-5.847,-10.000, 10.000, 9.740,-1.630, 6.567,-10.000,-4.539,-5.667,-6.977, 8.998, 6.530, 10.000, 9.304, 8.978,-9.303,-8.204,-5.445, 7.999, 2.272, 9.746, 0.081,-1.499,-9.298,-0.426,-1.665, 10.000,-3.112, 6.543, 9.528,-0.495, 7.865, 10.000, 5.364, 2.070,-4.212, 8.547,-0.748, 7.090, 10.000, 7.030, 5.233,5};
//
//    }



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

        //final int numThreads = Math.min(getRuntime().availableProcessors(), 32);
        final int numThreads = Math.min(getRuntime().availableProcessors(), 32);

        System.out.println("Requesting " + numThreads + " threads");
        final SimpleProblemWrapper problemWrapper;
        if(bestGuess == null)
            problemWrapper = new SimpleProblemWrapper();
        else
            problemWrapper = new SimpleProblemWrapper(){
                @Override
                public void initializePopulation(Population population) {

                    super.initializePopulation(population);

                    for (int scenario = 0; scenario < bestGuess.length; scenario++) {
                        final ESIndividualDoubleData individual = new ESIndividualDoubleData(
                                (ESIndividualDoubleData) population.get(0));
                        individual.setDoubleGenotype(bestGuess[scenario]);
                        individual.setDoublePhenotype(bestGuess[scenario]);

                        population.replaceIndividualAt(scenario,
                                individual);
                    }


                }
            };

        problemWrapper.setSimpleProblem(optimizationProblem);
        problemWrapper.setParallelThreads(numThreads);

        final ClusterBasedNichingEA optimizer = new ClusterBasedNichingEA();
        optimizer.setPopulationSize(populationSize);
//        final NelderMeadSimplex optimizer = new NelderMeadSimplex();
//        optimizer.setPopulationSize(20);
        problemWrapper.setDefaultRange(15);

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
