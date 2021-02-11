package uk.ac.ox.oxfish.maximization;

import com.univocity.parsers.csv.CsvWriter;
import eva2.OptimizerFactory;
import eva2.OptimizerRunnable;
import eva2.optimization.OptimizationParameters;
import eva2.optimization.operator.terminators.EvaluationTerminator;
import eva2.optimization.statistics.InterfaceStatisticsParameters;
import eva2.optimization.statistics.InterfaceTextListener;
import eva2.optimization.strategies.ClusterBasedNichingEA;
import eva2.problems.SimpleProblemWrapper;
import uk.ac.ox.oxfish.maximization.generic.AbstractLastStepFixedDataTarget;
import uk.ac.ox.oxfish.maximization.generic.FixedDataTarget;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;

import static java.lang.Runtime.getRuntime;
import static java.util.Arrays.stream;
import static java.util.stream.IntStream.rangeClosed;

@SuppressWarnings("UnstableApiUsage")
public class TunaMaximization {

    private static final boolean VERBOSE = false;
    private final Path calibrationFilePath;

    public TunaMaximization(final Path calibrationFilePath) {this.calibrationFilePath = calibrationFilePath;}

    public static void main(String[] args) {

        final int populationSize = 100;
        final int maxFitnessCalls = 2000;
        final int numEvaluationRuns = 10;

        final String folderName = stream(args).findFirst().orElse(LocalDate.now().toString());

        final Path basePath =
            Paths.get(System.getProperty("user.home"), "workspace", "tuna", "np", "calibrations", folderName);

        final Path calibrationFilePath = basePath.resolve("calibration.yaml");
        final Path calibratedScenarioPath = basePath.resolve("tuna_calibrated.yaml");
        final Path csvOutputPath = basePath.resolve("calibration_results.csv");

        final TunaMaximization tunaMaximization = new TunaMaximization(calibrationFilePath);

        final double[] solution = tunaMaximization.calibrate(populationSize, maxFitnessCalls);

//        final double[] solution =
//            { 5.239, 8.498,-7.969,-8.018, 0.793, 4.287, 7.936,-3.039,-4.793, 2.819,-3.052, 1.202,-1.329, 3.493, 2.809, 6.895, 5.718, 2.430, 10.000,-3.681,-2.818,-9.183, 0.581,-2.970, 4.954, 2.987,-5.269,-2.514, 6.830, 8.055, 8.288,-7.423, 0.873,-4.476, 2.838, 7.909, 10.000, 3.577, 10.000,-1.025,-5.071,-0.750, 4.821,-8.655,-1.300,-10.000,-8.555,-0.108, 10.000,-0.894,-7.470, 6.631,-3.934, 7.132};
//
//        tunaMaximization.saveCalibratedScenario(solution, calibratedScenarioPath);
//        final CsvWriter csvWriter = new CsvWriter(csvOutputPath.toFile(), new CsvWriterSettings());
//        tunaMaximization.evaluate(calibrationFilePath, csvWriter, numEvaluationRuns, solution);
    }

    public static void evaluate() {

    }

    @SuppressWarnings("SameParameterValue")
    private double[] calibrate(
        final int populationSize,
        final int maxFitnessCalls
    ) {

        final GenericOptimization optimizationProblem = makeGenericOptimizationProblem(calibrationFilePath);

        optimizationProblem.getTargets().stream()
            .filter(target -> target instanceof AbstractLastStepFixedDataTarget)
            .forEach(target -> ((AbstractLastStepFixedDataTarget) target).setVerbose(VERBOSE));

        final int numThreads = 4; //getRuntime().availableProcessors();

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

        try {
            File outputFolder = calibrationFilePath.getParent().toFile();
            Path outputPath = File.createTempFile("log_", ".md", outputFolder).toPath();
            try (
                final FileAndScreenWriter fileAndScreenWriter = new FileAndScreenWriter(outputPath)
            ) {
                runnable.setTextListener(fileAndScreenWriter);
                runnable.run();
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        return runnable.getDoubleSolution();

    }

    public void saveCalibratedScenario(double[] optimalParameters, Path calibratedScenarioPath) {
        try (FileWriter fileWriter = new FileWriter(calibratedScenarioPath.toFile())) {
            GenericOptimization optimization = makeGenericOptimizationProblem(calibrationFilePath);
            Scenario scenario = GenericOptimization.buildScenario(
                optimalParameters,
                Paths.get(optimization.getScenarioFile()).toFile(),
                optimization.getParameters()
            );
            new FishYAML().dump(scenario, fileWriter);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public void evaluate(
        Path calibrationFilePath,
        CsvWriter csvWriter,
        int numRuns,
        double[] solution
    ) {
        try {
            GenericOptimization optimization = makeGenericOptimizationProblem(calibrationFilePath);
            csvWriter.writeHeaders(
                "target_class",
                "target_name",
                "target_value",
                "run_number",
                "output_value",
                "error"
            );
            rangeClosed(1, numRuns).forEach(runNumber -> {
                final FishState fishState = runSimulation(optimization, solution, runNumber, numRuns);
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

    private GenericOptimization makeGenericOptimizationProblem(Path calibrationFile) {
        FishYAML yamlReader = new FishYAML();
        try (FileReader fileReader = new FileReader(calibrationFile.toFile())) {
            return yamlReader.loadAs(fileReader, GenericOptimization.class);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private FishState runSimulation(
        GenericOptimization optimization,
        double[] optimalParameters,
        int runNumber,
        int numRuns
    ) {
        final FishState fishState = new FishState(System.currentTimeMillis());
        try {
            Scenario scenario = GenericOptimization.buildScenario(
                optimalParameters,
                Paths.get(optimization.getScenarioFile()).toFile(),
                optimization.getParameters()
            );
            fishState.setScenario(scenario);
        } catch (FileNotFoundException e) {
            throw new IllegalStateException(e);
        }
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

    private static class FileAndScreenWriter implements InterfaceTextListener, Closeable {

        private final FileWriter fileWriter;

        FileAndScreenWriter(Path outputFile) throws IOException {
            this.fileWriter = new FileWriter(outputFile.toFile());
        }

        @Override
        public void close() throws IOException {
            fileWriter.close();
        }

        @Override
        public void print(String str) {
            System.out.println(str);
            try {
                fileWriter.write(str);
                fileWriter.flush();
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        public void println(String str) {
            print(str + "\n");
        }

    }

}
