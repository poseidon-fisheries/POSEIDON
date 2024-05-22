/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) -2024 CoHESyS Lab cohesys.lab@gmail.com
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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import uk.ac.ox.oxfish.maximization.GenericOptimization;
import uk.ac.ox.oxfish.maximization.generic.*;
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
import static uk.ac.ox.poseidon.common.core.csv.CsvParserUtil.recordStream;

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
            new ParameterExtractor(
                ImmutableSet.of(CalibratedParameter.class),
                LegacyParameterAddressBuilder::new
            )
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
