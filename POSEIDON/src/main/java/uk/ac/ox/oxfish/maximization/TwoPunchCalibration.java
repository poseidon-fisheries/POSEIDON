package uk.ac.ox.oxfish.maximization;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class TwoPunchCalibration {

    public static void main(final String[] args) throws IOException {
        runAll(
            Paths.get(args[0]),
            Integer.parseInt(args[1]),
            args.length > 2 ? Integer.parseInt(args[2]) : 2000,
            args.length > 3 ? Integer.parseInt(args[3]) : 5000
        );
    }

    private static void runAll(
        final Path calibrationFile,
        final int nProcs,
        final int stepOneFitnessCalls,
        final int stepTwoFitnessCalls
    ) throws IOException {
        //run GA
        final double[] gaSolution = stepOne(calibrationFile, nProcs, stepOneFitnessCalls);
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
        final double[] localSolution = stepTwo(
            localCalibrationFile,
            nProcs,
            stepTwoFitnessCalls
        );
        writeSolutionOut(localCalibrationFile, localSolution, "local_solution.txt");

        //run once again locally
        final TunaEvaluator evaluator = new TunaEvaluator(localCalibrationFile, localSolution);
        evaluator.setNumRuns(10);
        evaluator.setParallel(false);
        evaluator.run();


    }

    private static double[] stepOne(
        final Path calibrationFile,
        final int nProcs,
        final int maxFitnessCalls
    ) throws IOException {

        final TunaCalibrationConsole firstStep = new TunaCalibrationConsole();
        firstStep.setLocalSearch(false);
        firstStep.setPSO(false);
        firstStep.setPopulationSize(200);
        firstStep.setMaxProcessorsToUse(nProcs);
        firstStep.setNumberOfRunsPerSettingOverride(1);
        firstStep.setMaxFitnessCalls(maxFitnessCalls);
        firstStep.setParameterRange(15);
        firstStep.setRunNickName("global");
        firstStep.setPathToCalibrationYaml(calibrationFile.toAbsolutePath().toString());
//        --pop 200 --maxProcs 4 --runs 3000 --range 10 --runsPerSetting 1
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

    public static double[] stepTwo(
        final Path calibrationFile,
        final int nProcs,
        final int maxFitnessCalls
    ) throws IOException {

        final TunaCalibrationConsole secondStep = new TunaCalibrationConsole();
        secondStep.setLocalSearch(false);
        secondStep.setPSO(true);
        secondStep.setBestGuessesTextFile(
            calibrationFile.getParent().resolve("zeros.txt").toFile().getAbsolutePath()
        );
        secondStep.setPopulationSize(50);
        secondStep.setMaxProcessorsToUse(nProcs);
        secondStep.setNumberOfRunsPerSettingOverride(2);
        secondStep.setMaxFitnessCalls(maxFitnessCalls);
        secondStep.setParameterRange(17);
        secondStep.setPathToCalibrationYaml(calibrationFile.toAbsolutePath().toString());
        secondStep.setRunNickName("local");
        return secondStep.generateCalibratorProblem().run();
    }

}
