package uk.ac.ox.oxfish.maximization;

import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;
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

        final int populationSize = 200;
        final int maxFitnessCalls = 5000;
        final int numEvaluationRuns = 10;

        final String folderName = stream(args).findFirst().orElse(LocalDate.now().toString());

        final Path basePath =
            Paths.get(System.getProperty("user.home"), "workspace", "tuna", "np", "calibrations", folderName);

        final Path calibrationFilePath = basePath.resolve("calibration.yaml");
        final Path calibratedScenarioPath = basePath.resolve("tuna_calibrated.yaml");
        final Path csvOutputPath = basePath.resolve("calibration_results.csv");

        final TunaMaximization tunaMaximization = new TunaMaximization(calibrationFilePath);

        final double[] solution = tunaMaximization.calibrate(populationSize, maxFitnessCalls);

        // final double[] solution =
        //    {-1.7023085096785586, 3.390807334225775, -10.0, 9.832137275068147, -8.905492349990276, 10.0, -8.86538894532567, -0.7701673884919678, -2.1896491189876675, 5.971443820255731, 5.766108791252238, -10.0, -1.5699859246913923, 10.0, 1.9001353296628898, 6.956476022912932, 10.0, -1.3549025645239539, -0.9278451842185644, 6.058659044014105, 7.497317002392419, -1.6808634972659506, -0.19964991580650704, 2.2660920630401367, -3.1686751990893876, -2.8198705752909383, -10.0, -6.190012113107348, -8.937442934688907, -9.762822378350377, 2.005936971572968, 2.556394676644074, -9.954715705858861, -4.294454164143142, 4.318319978320221, 3.668671020316231, -5.633206242658223, 3.57950537162915, 10.0, 4.816721245334941, 8.392658938635206, 3.6703662669225983, 3.4102903234775646, -4.131573574382304, -0.54857221937721, -7.658529498809894, 7.654988910506763, -8.404991144164065, -3.376850929825255, -8.249628412876314, -10.0, 10.0, -7.579135096893957, 5.431430212071597};

        // tunaMaximization.saveCalibratedScenario(solution, calibratedScenarioPath);
        // final CsvWriter csvWriter = new CsvWriter(csvOutputPath.toFile(), new CsvWriterSettings());
        // tunaMaximization.evaluate(calibrationFilePath, csvWriter, numEvaluationRuns, solution);
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

        final int numThreads = Math.min(getRuntime().availableProcessors(), 16);

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
            System.out.println("Logging to: " + outputPath);
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
