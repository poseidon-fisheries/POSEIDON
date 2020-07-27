package uk.ac.ox.oxfish.maximization;

import com.google.common.io.Files;
import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;
import eva2.OptimizerFactory;
import eva2.OptimizerRunnable;
import eva2.optimization.OptimizationParameters;
import eva2.optimization.statistics.InterfaceStatisticsParameters;
import eva2.optimization.statistics.InterfaceTextListener;
import eva2.optimization.strategies.ClusterBasedNichingEA;
import eva2.problems.SimpleProblemWrapper;
import uk.ac.ox.oxfish.maximization.generic.FixedDataLastStepTarget;
import uk.ac.ox.oxfish.maximization.generic.FixedDataTarget;
import uk.ac.ox.oxfish.maximization.generic.ScaledFixedDataLastStepTarget;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.lang.Runtime.getRuntime;
import static java.util.stream.IntStream.rangeClosed;

@SuppressWarnings("UnstableApiUsage")
public class TunaMaximization {

    private static final int GB = 1024 * 1024 * 1024;
    private final Path calibrationFilePath;

    public TunaMaximization(final Path calibrationFilePath) {this.calibrationFilePath = calibrationFilePath;}

    public static void main(String[] args) {

        ScaledFixedDataLastStepTarget.VERBOSE = true;
        FixedDataLastStepTarget.VERBOSE = true;

        final int minMemoryPerThread = 1 * GB;
        final int populationSize = 500;
        final Path basePath =
            Paths.get(System.getProperty("user.home"), "workspace", "tuna", "np", "calibrations", "2020-07-15");

        final Path calibrationFilePath = basePath.resolve("calibration.yaml");
        final Path calibratedScenarioPath = basePath.resolve("tuna_calibrated.yaml");
        final Path csvOutputPath = basePath.resolve("calibration_results.csv");

        final TunaMaximization tunaMaximization = new TunaMaximization(calibrationFilePath);

        final double[] solution = tunaMaximization.calibrate(minMemoryPerThread, populationSize);

//        final double[] solution =
//            {0.086, 3.238, -7.165, -6.770, -0.893, 10.000, -8.301, -4.487, 3.181, -6.490, -8.782, 1.569, 5.822};

        tunaMaximization.saveCalibratedScenario(solution, calibratedScenarioPath);
        final CsvWriter csvWriter = new CsvWriter(csvOutputPath.toFile(), new CsvWriterSettings());
        tunaMaximization.evaluate(calibrationFilePath, csvWriter, 30, solution);
    }

    private double[] calibrate(
        int minMemoryPerThread,
        int populationSize
    ) {

        final GenericOptimization optimizationProblem = makeGenericOptimizationProblem(calibrationFilePath);

        final int numThreads = 8; //Math.min(
//            (int) getRuntime().maxMemory() / minMemoryPerThread,
//            getRuntime().availableProcessors()
//        );

        System.out.println("Requesting " + numThreads + " threads");

        SimpleProblemWrapper problemWrapper = new SimpleProblemWrapper();
        problemWrapper.setSimpleProblem(optimizationProblem);
        problemWrapper.setParallelThreads(numThreads);

        ClusterBasedNichingEA optimizer = new ClusterBasedNichingEA();
        optimizer.setPopulationSize(populationSize);

        final OptimizationParameters optimizationParameters =
            OptimizerFactory.makeParams(optimizer, populationSize, problemWrapper);

        OptimizerRunnable runnable = new OptimizerRunnable(optimizationParameters, "");
        runnable.setOutputFullStatsToText(true);
        runnable.setVerbosityLevel(InterfaceStatisticsParameters.OutputVerbosity.ALL);
        runnable.setOutputTo(InterfaceStatisticsParameters.OutputTo.WINDOW);

        String name = Files.getNameWithoutExtension(calibrationFilePath.getFileName().toString());
        final Path outputFile = calibrationFilePath.getParent().resolve(name + "_log.md");
        try (final FileAndScreenWriter fileAndScreenWriter = new FileAndScreenWriter(outputFile)) {
            runnable.setTextListener(fileAndScreenWriter);
            runnable.run();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        return runnable.getDoubleSolution();

    }

    public void saveCalibratedScenario(double[] solution, Path calibratedScenarioPath) {
        try (FileWriter fileWriter = new FileWriter(calibratedScenarioPath.toFile())) {
            GenericOptimization optimization = makeGenericOptimizationProblem(calibrationFilePath);
            final Scenario scenario = optimization.buildScenario(solution);
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
        double[] solution,
        int runNumber,
        int numRuns
    ) {
        final FishState fishState = new FishState(System.currentTimeMillis());
        try {
            final Scenario scenario = optimization.buildScenario(solution);
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

        @Override public void close() throws IOException {
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
