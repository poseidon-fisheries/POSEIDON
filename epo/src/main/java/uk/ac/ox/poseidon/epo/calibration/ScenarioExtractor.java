/*
 * POSEIDON, an agent-based model of fisheries
 * Copyright (c) 2024-2024 CoHESyS Lab cohesys.lab@gmail.com
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package uk.ac.ox.poseidon.epo.calibration;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import uk.ac.ox.oxfish.maximization.GenericOptimization;
import uk.ac.ox.oxfish.maximization.SolutionExtractor;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.io.Files.getFileExtension;

public class ScenarioExtractor implements Supplier<Scenario> {

    private final Path calibrationLogFile;
    private final Path calibrationFolder;

    public ScenarioExtractor(
        final Path calibrationFolder,
        final Path calibrationLogFile
    ) {
        this.calibrationFolder = calibrationFolder;
        this.calibrationLogFile = calibrationLogFile;
    }

    private static Path findCalibrationFile(final Path folder) {
        try (final Stream<Path> paths = Files.list(folder)) {
            final ImmutableList<Path> calibrationFiles = paths
                .filter(path -> getFileExtension(path.toString()).equals("yaml"))
                .filter(ScenarioExtractor::isCalibrationFile)
                .collect(toImmutableList());
            checkState(!calibrationFiles.isEmpty(), "No calibration files found in %s", folder);
            checkState(calibrationFiles.size() == 1, "More than one calibration files found in %s", folder);
            return calibrationFiles.get(0);
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Scenario makeScenario(
        final GenericOptimization optimization,
        final double[] optimalParameters
    ) {
        try {
            return GenericOptimization.buildScenario(
                optimalParameters,
                Paths.get(optimization.getScenarioFile()).toFile(),
                optimization.getParameters()
            );
        } catch (final FileNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    private static boolean isCalibrationFile(final Path path) {
        try (final Stream<String> lines = Files.lines(path)) {
            return lines
                .findFirst()
                .filter(line -> line.equals("!!uk.ac.ox.oxfish.maximization.GenericOptimization"))
                .isPresent();
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public Scenario getAndWriteToFile(final String outputFileName) {
        final Scenario scenario = get();
        writeScenarioToFile(scenario, outputFileName);
        return scenario;
    }

    @Override
    public Scenario get() {
        final Path calibrationFilePath = findCalibrationFile(calibrationFolder);
        final Path logFilePath = calibrationFolder.resolve(calibrationLogFile);
        final double[] solution = new SolutionExtractor(logFilePath).bestSolution().getKey();
        final GenericOptimization optimization = GenericOptimization.fromFile(calibrationFilePath);
        return makeScenario(optimization, solution);
    }

    private void writeScenarioToFile(
        final Scenario scenario,
        final String outputFileName
    ) {
        final File outputFile = calibrationFolder.resolve(outputFileName).toFile();
        try (final Writer writer = new FileWriter(outputFile)) {
            new FishYAML().dump(scenario, writer);
        } catch (final IOException e) {
            throw new IllegalStateException("Error while writing file: " + outputFile, e);
        }
    }
}
