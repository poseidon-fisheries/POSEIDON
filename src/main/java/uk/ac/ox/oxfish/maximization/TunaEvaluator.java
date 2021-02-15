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
    private int numRuns = 10;

    public TunaEvaluator(Path calibrationFilePath, double[] solution) {
        this.calibrationFilePath = calibrationFilePath;
        this.solution = solution;
    }

    @SuppressWarnings("unused")
    public int getNumRuns() {
        return numRuns;
    }

    @SuppressWarnings("unused")
    public void setNumRuns(int numRuns) {
        this.numRuns = numRuns;
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
        fishState.setScenario(makeScenario(optimization, optimalParameters));
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

}
