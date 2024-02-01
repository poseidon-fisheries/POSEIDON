package uk.ac.ox.oxfish.parameters;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import uk.ac.ox.oxfish.maximization.GenericOptimization;
import uk.ac.ox.oxfish.maximization.generic.DataTarget;
import uk.ac.ox.oxfish.maximization.generic.FixedDataTargetGenerator;
import uk.ac.ox.oxfish.maximization.generic.HardEdgeOptimizationParameter;
import uk.ac.ox.oxfish.maximization.generic.OptimizationParameter;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.parameters.CalibratedParameter;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.Streams.stream;
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
            new ParameterExtractor<>(CalibratedParameter.class)
                .getParameters(scenario)
                .map(parameter -> new HardEdgeOptimizationParameter(
                    parameter.getAddress(),
                    parameter.getObject().getMinimum(),
                    parameter.getObject().getMaximum(),
                    parameter.getObject().getHardMinimum() >= 0,
                    false,
                    parameter.getObject().getHardMinimum(),
                    parameter.getObject().getHardMaximum()
                ))
                .collect(Collectors.toList());
        final ImmutableMap<String, FixedDataTargetGenerator> targetGenerators =
            stream(ServiceLoader.load(FixedDataTargetGenerator.class))
                .collect(toImmutableMap(
                    FixedDataTargetGenerator::getName,
                    Function.identity()
                ));
        final ImmutableList<DataTarget> targets =
            recordStream(targetsFile)
                .filter(r -> r.getInt("year") == targetYear)
                .map(r ->
                    Objects
                        .requireNonNull(
                            targetGenerators.get(r.getString("errorMeasure"))
                        )
                        .create(
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
