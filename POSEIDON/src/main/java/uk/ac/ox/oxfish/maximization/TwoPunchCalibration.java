package uk.ac.ox.oxfish.maximization;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.PathConverter;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;

import static java.lang.Runtime.getRuntime;

public class TwoPunchCalibration {

    @Parameter(names = {"-p", "--parallel_threads"})
    private int parallelThreads = getRuntime().availableProcessors();

    @Parameter(names = {"-g", "--max_global_calls"})
    private int maxGlobalCalls = 2000;

    @Parameter(names = {"-l", "--max_local_calls"})
    private int maxLocalCalls = 5000;

    @Parameter(converter = PathConverter.class)
    private Path calibrationFile;

    public static void main(final String[] args) throws IOException {
        final TwoPunchCalibration twoPunchCalibration = new TwoPunchCalibration();
        JCommander.newBuilder()
            .addObject(twoPunchCalibration)
            .build()
            .parse(args);
        twoPunchCalibration.runAll();
    }

    private void runAll() throws IOException {
        //run GA
        final double[] gaSolution = stepOne();
        writeSolutionOut(calibrationFile, gaSolution, "ga_solution.txt");
        final double[] zeros = new double[gaSolution.length];
        Arrays.fill(zeros, 0d);
        writeSolutionOut(calibrationFile, zeros, "zeros.txt");
        //run PSO
        GenericOptimization.buildLocalCalibrationProblem(
            calibrationFile,
            gaSolution,
            "local_calibration.yaml",
            .2
        );
        final Path localCalibrationFile = calibrationFile.getParent().resolve("local_calibration.yaml");
        final double[] localSolution = stepTwo();
        writeSolutionOut(localCalibrationFile, localSolution, "local_solution.txt");

        //run once again locally
        final TunaEvaluator evaluator = new TunaEvaluator(localCalibrationFile, localSolution);
        evaluator.setNumRuns(10);
        evaluator.setParallel(false);
        evaluator.run();
    }

    private double[] stepOne() throws IOException {

        final TunaCalibrationConsole firstStep = new TunaCalibrationConsole();
        firstStep.setLocalSearch(false);
        firstStep.setPSO(false);
        firstStep.setPopulationSize(200);
        firstStep.setMaxProcessorsToUse(parallelThreads);
        firstStep.setNumberOfRunsPerSettingOverride(1);
        firstStep.setMaxFitnessCalls(maxGlobalCalls);
        firstStep.setParameterRange(15);
        firstStep.setRunNickName("global");
        firstStep.setPathToCalibrationYaml(calibrationFile.toAbsolutePath().toString());
        return firstStep.generateCalibratorProblem().run();
    }

    private static void writeSolutionOut(
        final Path calibrationFile,
        final double[] gaSolution,
        final String solutionName
    ) throws IOException {
        final FileWriter writer = new FileWriter(calibrationFile.getParent().resolve(solutionName).toFile());
        writer.write(Double.toString(gaSolution[0]));
        for (int i = 1; i < gaSolution.length; i++) {
            writer.write(",");
            writer.write(Double.toString(gaSolution[i]));
        }
        writer.close();
    }

    private double[] stepTwo() throws IOException {
        final TunaCalibrationConsole secondStep = new TunaCalibrationConsole();
        secondStep.setLocalSearch(false);
        secondStep.setPSO(true);
        secondStep.setBestGuessesTextFile(
            calibrationFile.getParent().resolve("zeros.txt").toFile().getAbsolutePath()
        );
        secondStep.setPopulationSize(50);
        secondStep.setMaxProcessorsToUse(parallelThreads);
        secondStep.setNumberOfRunsPerSettingOverride(2);
        secondStep.setMaxFitnessCalls(maxLocalCalls);
        secondStep.setParameterRange(17);
        secondStep.setPathToCalibrationYaml(calibrationFile.toAbsolutePath().toString());
        secondStep.setRunNickName("local");
        return secondStep.generateCalibratorProblem().run();
    }

    public int getParallelThreads() {
        return parallelThreads;
    }

    @SuppressWarnings("unused")
    public void setParallelThreads(final int parallelThreads) {
        this.parallelThreads = parallelThreads;
    }

    public int getMaxLocalCalls() {
        return maxLocalCalls;
    }

    @SuppressWarnings("unused")
    public void setMaxLocalCalls(final int maxLocalCalls) {
        this.maxLocalCalls = maxLocalCalls;
    }

    public int getMaxGlobalCalls() {
        return maxGlobalCalls;
    }

    @SuppressWarnings("unused")
    public void setMaxGlobalCalls(final int maxGlobalCalls) {
        this.maxGlobalCalls = maxGlobalCalls;
    }

    @SuppressWarnings("unused")
    public Path getCalibrationFile() {
        return calibrationFile;
    }

    @SuppressWarnings("unused")
    public void setCalibrationFile(final Path calibrationFile) {
        this.calibrationFile = calibrationFile;
    }

}
