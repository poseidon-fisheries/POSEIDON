package uk.ac.ox.oxfish.maximization;

import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;
import uk.ac.ox.oxfish.maximization.generic.FixedDataTarget;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.Scenario;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.util.stream.IntStream.rangeClosed;

public class TunaEvaluator implements Runnable {

    private final Path calibrationFilePath;
    private final double[] solution;
    private int numRuns = 1;

    public TunaEvaluator(Path calibrationFilePath, double[] solution) {
        this.calibrationFilePath = calibrationFilePath;
        this.solution = solution;
    }

    public static void main(String[] args) {

        double[] solution = {-2.215, -10.000, 9.917, 2.297, -8.628, 4.953, 4.575, -8.384, -10.000, -8.177, 10.000, 2.394, -7.054, 4.151, -10.000, 10.000, -10.000, 10.000, 10.000, 8.226, 9.795, 8.633, -10.000, -10.000, 6.368, 7.868, -7.203, -6.451, -1.582, 8.191, -0.469, -4.748, 3.718, -5.544, 9.824, -1.592, 9.451, 8.623, 2.879, -7.875, 10.000, -8.370, 4.009, -6.165, 6.632, 9.039, 3.556, -6.134, -6.509, -2.655, -10.000, 9.821, -8.838, 9.209};
        Path baseFolderPath = Paths
            .get(System.getProperty("user.home"), "workspace", "tuna", "np", "calibrations");

        new TunaEvaluator(
            baseFolderPath.resolve("2021-02-16_20.42.51/calibration.yaml"),
            solution
        ).run();

    }

    @Override
    public void run() {

        final Path csvOutputFilePath = calibrationFilePath.getParent().resolve("evaluation_results.csv");
        final CsvWriter csvWriter = new CsvWriter(csvOutputFilePath.toFile(), new CsvWriterSettings());

        try {
            GenericOptimization optimization = GenericOptimization.fromFile(calibrationFilePath);
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

    private FishState runSimulation(
        GenericOptimization optimization,
        double[] optimalParameters,
        int runNumber,
        int numRuns
    ) {
        final FishState fishState = new FishState(System.currentTimeMillis());
        Scenario scenario = makeScenario(optimization, optimalParameters);
        fishState.setScenario(scenario);
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

    Scenario makeScenario(
        GenericOptimization optimization,
        double[] optimalParameters
    ) {
        try {
            return GenericOptimization.buildScenario(
                optimalParameters,
                Paths.get(optimization.getScenarioFile()).toFile(),
                optimization.getParameters()
            );
        } catch (FileNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    @SuppressWarnings("unused")
    public int getNumRuns() {
        return numRuns;
    }

    @SuppressWarnings("unused")
    public void setNumRuns(int numRuns) {
        this.numRuns = numRuns;
    }

}
