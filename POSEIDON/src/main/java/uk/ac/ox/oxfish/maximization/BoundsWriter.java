package uk.ac.ox.oxfish.maximization;

import com.google.common.collect.ImmutableList;
import uk.ac.ox.oxfish.maximization.generic.HardEdgeOptimizationParameter;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.poseidon.common.core.csv.CsvParserUtil;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class BoundsWriter {

    private static final String DEFAULT_CALIBRATION_FILE_NAME = "calibration.yaml";
    private static final String DEFAULT_LOG_FILE_NAME = "calibration_log.md";
    private static final String DEFAULT_OUTPUT_FILE_NAME = "bounds.csv";

    public static void writeBounds(final Path calibrationFolder) {
        writeBounds(
            calibrationFolder.resolve(DEFAULT_CALIBRATION_FILE_NAME),
            calibrationFolder.resolve(DEFAULT_LOG_FILE_NAME),
            calibrationFolder.resolve(DEFAULT_OUTPUT_FILE_NAME)
        );
    }

    private static void writeBounds(
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

}
