package uk.ac.ox.oxfish.parameters;

import com.google.common.collect.ImmutableList;
import uk.ac.ox.oxfish.maximization.GenericOptimization;
import uk.ac.ox.oxfish.maximization.generic.DataTarget;
import uk.ac.ox.oxfish.maximization.generic.OptimizationParameter;
import uk.ac.ox.oxfish.maximization.generic.SmapeDataTarget;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static uk.ac.ox.oxfish.utility.csv.CsvParserUtil.recordStream;

public class CalibrationGenerator {

    public void generateCalibration(
        final Scenario scenario,
        final Path calibrationFolder,
        final Path targetsFile,
        final int targetYear,
        final int numRunsPerSetting,
        final int numSimulatedYears
    ) {
        final FishYAML yaml = new FishYAML();
        final Path scenarioFile = calibrationFolder.resolve("scenario.yaml");
        yaml.dump(scenario, scenarioFile);
        final List<OptimizationParameter> parameters =
            new ParameterExtractor()
                .getParameters(scenario)
                .collect(Collectors.toList());
        final ImmutableList<DataTarget> targets =
            recordStream(targetsFile)
                .filter(r -> r.getInt("year") == targetYear)
                .map(r -> new SmapeDataTarget(
                    r.getString("columnName"),
                    r.getDouble("fixedTarget")
                ))
                .collect(toImmutableList());
        final GenericOptimization genericOptimization =
            new GenericOptimization(
                scenarioFile.toString(),
                parameters,
                targets,
                numRunsPerSetting,
                numSimulatedYears
            );
        final Path calibrationFile = calibrationFolder.resolve("calibration.yaml");
        yaml.dump(genericOptimization, calibrationFile);
    }
}
