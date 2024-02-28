package uk.ac.ox.oxfish.maximization;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.PathConverter;
import com.google.common.collect.ImmutableList;
import uk.ac.ox.oxfish.maximization.generic.HardEdgeOptimizationParameter;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.poseidon.common.core.csv.CsvParserUtil;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class BoundsWriter implements Runnable {

    @Parameter(names = "--output_file")
    private String outputFileName = "bounds.csv";
    @Parameter(names = "--calibration_file")
    private String calibrationFileName = "calibration.yaml";
    @Parameter(names = "--log_file")
    private String logFileName = "calibration_log.md";
    @Parameter(converter = PathConverter.class)
    private Path calibrationFolder;

    public static void main(final String[] args) {
        final Runnable boundsWriter = new BoundsWriter();
        JCommander.newBuilder()
            .addObject(boundsWriter)
            .build()
            .parse(args);
        boundsWriter.run();
    }

    public static void writeBounds(
        final GenericOptimization genericOptimization,
        final double[] solution,
        final Path outputFile
    ) {
        final Scenario scenario = genericOptimization.buildScenario(solution);
        final Stream<List<?>> rows =
            genericOptimization.getParameters()
                .stream()
                .filter(HardEdgeOptimizationParameter.class::isInstance)
                .map(HardEdgeOptimizationParameter.class::cast)
                .map(p -> ImmutableList.of(
                    p.getAddressToModify(),
                    p.getMinimum(),
                    p.getMaximum(),
                    p.getHardMinimum(),
                    p.getHardMaximum(),
                    p.getValue(scenario)
                ));
        CsvParserUtil.writeRows(
            outputFile,
            ImmutableList.of(
                "parameter",
                "soft_minimum",
                "soft_maximum",
                "hard_minimum",
                "hard_maximum",
                "value"
            ),
            rows::iterator
        );
    }

    public void run() {
        writeBounds(
            getCalibrationFolder().resolve(getCalibrationFileName()),
            getCalibrationFolder().resolve(getLogFileName()),
            getCalibrationFolder().resolve(getOutputFileName())
        );
    }

    public void writeBounds(
        final Path calibrationFile,
        final Path logFile,
        final Path outputFile
    ) {
        writeBounds(
            GenericOptimization.fromFile(calibrationFile),
            new SolutionExtractor(logFile).bestSolution().getKey(),
            outputFile
        );
    }

    public String getOutputFileName() {
        return outputFileName;
    }

    @SuppressWarnings("unused")
    public void setOutputFileName(final String outputFileName) {
        this.outputFileName = outputFileName;
    }

    public String getCalibrationFileName() {
        return calibrationFileName;
    }

    @SuppressWarnings("unused")
    public void setCalibrationFileName(final String calibrationFileName) {
        this.calibrationFileName = calibrationFileName;
    }

    public String getLogFileName() {
        return logFileName;
    }

    @SuppressWarnings("unused")
    public void setLogFileName(final String logFileName) {
        this.logFileName = logFileName;
    }

    public Path getCalibrationFolder() {
        return calibrationFolder;
    }

    @SuppressWarnings("unused")
    public void setCalibrationFolder(final Path calibrationFolder) {
        this.calibrationFolder = calibrationFolder;
    }

}
