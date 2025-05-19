/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024-2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.ac.ox.oxfish.parameters;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.PathConverter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import uk.ac.ox.oxfish.maximization.GenericOptimization;
import uk.ac.ox.oxfish.maximization.generic.*;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.model.scenario.ScenarioSupplier;
import uk.ac.ox.oxfish.utility.parameters.CalibratedParameter;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.Streams.stream;
import static uk.ac.ox.poseidon.common.core.csv.CsvParserUtil.recordStream;

public class CalibrationGenerator implements Runnable {

    private static final String CALIBRATION_FILENAME = "calibration.yaml";
    private static final String SCENARIO_FILENAME = "scenario.yaml";
    private static final String ERROR_MEASURE_COLUMN = "errorMeasure";
    private static final String TARGET_NAME_COLUMN = "columnName";
    private static final String TARGET_VALUE_COLUMN = "fixedTarget";
    private static final String YEAR_COLUMN = "year";
    @Parameter(names = {"-s", "--scenario_name"})
    private String scenarioName;
    @Parameter(names = {"-of", "--output_folder"}, converter = PathConverter.class)
    private Path outputFolder;
    @Parameter(names = {"-tf", "--targets_file"}, converter = PathConverter.class)
    private Path targetsFile;
    @Parameter(names = {"-ty", "--target_year"})
    private int targetYear = 2022;
    @Parameter(names = {"-ny", "--num_simulated_years"})
    private int numSimulatedYears = 2;
    @Parameter(names = {"-r", "--num_runs_per_setting"})
    private int numRunsPerSetting = 1;

    /**
     * If you want to run this directly from IntelliJ and use a scenario that's defined in a
     * separate module (e.g. "EPO Path Planner Abundance" that is defined in the POSEIDON.epo
     * module), you need to change the setting in *Settings | Build, Execution, Deployment | Build
     * Tools | Gradle* to use IntelliJ IDEA instead of Gradle for building and running. Only then
     * will the module class path specified in the run configuration (e.g., `-cp POSEIDON.epo.main`)
     * be honored. See: <a
     * href="https://youtrack.jetbrains.com/issue/IDEA-220528">https://youtrack.jetbrains
     * .com/issue/IDEA-220528</a>.
     */
    public static void main(final String[] args) {
        final Runnable calibrationGenerator = new CalibrationGenerator();
        JCommander.newBuilder()
            .addObject(calibrationGenerator)
            .build()
            .parse(args);
        calibrationGenerator.run();
    }

    public String getScenarioName() {
        return scenarioName;
    }

    public void setScenarioName(final String scenarioName) {
        this.scenarioName = scenarioName;
    }

    public Path getOutputFolder() {
        return outputFolder;
    }

    public void setOutputFolder(final Path outputFolder) {
        this.outputFolder = outputFolder;
    }

    public Path getTargetsFile() {
        return targetsFile;
    }

    public void setTargetsFile(final Path targetsFile) {
        this.targetsFile = targetsFile;
    }

    public int getTargetYear() {
        return targetYear;
    }

    public void setTargetYear(final int targetYear) {
        this.targetYear = targetYear;
    }

    public int getNumSimulatedYears() {
        return numSimulatedYears;
    }

    public void setNumSimulatedYears(final int numSimulatedYears) {
        this.numSimulatedYears = numSimulatedYears;
    }

    public int getNumRunsPerSetting() {
        return numRunsPerSetting;
    }

    public void setNumRunsPerSetting(final int numRunsPerSetting) {
        this.numRunsPerSetting = numRunsPerSetting;
    }

    @Override
    public void run() {
        final FishYAML yaml = new FishYAML();
        final Path scenarioFile = outputFolder.resolve(SCENARIO_FILENAME);
        final Scenario scenario = newScenario();
        yaml.dump(scenario, scenarioFile);
        final ParameterExtractor parameterExtractor = new ParameterExtractor(
            ImmutableSet.of(CalibratedParameter.class),
            BeanParameterAddressBuilder::new
        );
        final List<OptimizationParameter> parameters =
            parameterExtractor
                .getParameters(scenario)
                .filter(extractedParameter ->
                    extractedParameter.getObject() instanceof CalibratedParameter
                )
                .map(parameter -> {
                    final CalibratedParameter calibratedParameter =
                        (CalibratedParameter) parameter.getObject();
                    return new HardEdgeOptimizationParameter(
                        parameter.getAddress(),
                        calibratedParameter.getMinimum(),
                        calibratedParameter.getMaximum(),
                        false,
                        calibratedParameter.getHardMinimum(),
                        calibratedParameter.getHardMaximum()
                    );
                })
                .collect(Collectors.toList());
        final ImmutableMap<String, FixedDataTargetGenerator> targetGenerators =
            stream(ServiceLoader.load(FixedDataTargetGenerator.class))
                .collect(toImmutableMap(
                    FixedDataTargetGenerator::getName,
                    Function.identity()
                ));
        final ImmutableList<DataTarget> targets =
            recordStream(targetsFile)
                .filter(r -> r.getInt(YEAR_COLUMN) == targetYear)
                .map(r ->
                    Objects
                        .requireNonNull(
                            targetGenerators.get(r.getString(ERROR_MEASURE_COLUMN))
                        )
                        .create(
                            r.getString(TARGET_NAME_COLUMN),
                            r.getDouble(TARGET_VALUE_COLUMN)
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
        final Path calibrationFile = outputFolder.resolve(CALIBRATION_FILENAME);
        yaml.dump(genericOptimization, calibrationFile);
    }

    private Scenario newScenario() {
        return stream(ServiceLoader.load(ScenarioSupplier.class))
            .filter(scenarioSupplier ->
                Objects.equals(scenarioSupplier.getScenarioName(), scenarioName)
            )
            .findFirst()
            .map(Supplier::get)
            .orElseThrow(() -> new RuntimeException(MessageFormat.format(
                "Scenario not found: \"{0}\"",
                scenarioName
            )));
    }
}
