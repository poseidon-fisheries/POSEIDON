/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024 CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.poseidon.epo.calibration;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.PathConverter;
import com.google.common.collect.ImmutableList;
import uk.ac.ox.oxfish.maximization.GenericOptimization;
import uk.ac.ox.oxfish.maximization.generic.SimpleOptimizationParameter;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static java.lang.Runtime.getRuntime;
import static uk.ac.ox.poseidon.epo.calibration.Calibrator.CALIBRATION_FILENAME;

public class TwoPunchCalibration implements JCommanderRunnable {
    @Parameter(names = {"-p", "--parallel_threads"})
    private int parallelThreads = getRuntime().availableProcessors();
    @Parameter(names = {"-g", "--max_global_calls"})
    private int maxGlobalCalls = 2000;
    @Parameter(names = {"-l", "--max_local_calls"})
    private int maxLocalCalls = 5000;
    @Parameter(converter = PathConverter.class)
    private Path rootCalibrationFolder;
    @Parameter(names = {"-f", "--calibration_file"}, converter = PathConverter.class)
    private Path calibrationFile = Paths.get(CALIBRATION_FILENAME);
    @Parameter(names = {"-s", "--seed_scenarios"}, converter = PathConverter.class)
    private List<Path> seedScenarios = ImmutableList.of();

    public static void main(final String[] args) {
        new TwoPunchCalibration().run(args);
    }

    @SuppressWarnings("unused")
    public Path getRootCalibrationFolder() {
        return rootCalibrationFolder;
    }

    @SuppressWarnings("unused")
    public void setRootCalibrationFolder(final Path rootCalibrationFolder) {
        this.rootCalibrationFolder = rootCalibrationFolder;
    }

    @SuppressWarnings("unused")
    public List<Path> getSeedScenarios() {
        return seedScenarios;
    }

    @SuppressWarnings("unused")
    public void setSeedScenarios(final List<Path> seedScenarios) {
        this.seedScenarios = seedScenarios;
    }

    public int getParallelThreads() {
        return parallelThreads;
    }

    @SuppressWarnings("unused")
    public void setParallelThreads(final int parallelThreads) {
        this.parallelThreads = parallelThreads;
    }

    public int getMaxLocalCalls() {
        return maxLocalCalls;
    }

    @SuppressWarnings("unused")
    public void setMaxLocalCalls(final int maxLocalCalls) {
        this.maxLocalCalls = maxLocalCalls;
    }

    public int getMaxGlobalCalls() {
        return maxGlobalCalls;
    }

    @SuppressWarnings("unused")
    public void setMaxGlobalCalls(final int maxGlobalCalls) {
        this.maxGlobalCalls = maxGlobalCalls;
    }

    @SuppressWarnings("unused")
    public Path getCalibrationFile() {
        return calibrationFile;
    }

    @SuppressWarnings("unused")
    public void setCalibrationFile(final Path calibrationFile) {
        this.calibrationFile = calibrationFile;
    }

    @Override
    public void run() {
        final Calibrator.Result globalCalibratorResult =
            new Calibrator(
                "global",
                200,
                maxGlobalCalls,
                15,
                parallelThreads,
                false,
                rootCalibrationFolder,
                rootCalibrationFolder.resolve(calibrationFile),
                seedScenarios,
                new Calibrator.ClusterBasedNichingEAInitializer()
            ).calibrate();

        final GenericOptimization localCalibrationProblem =
            buildLocalCalibrationProblem(globalCalibratorResult.getSolution(), 0.2);
        localCalibrationProblem.setRunsPerSetting(2);

        final Calibrator.Result localCalibratorResult = new Calibrator(
            "local",
            50,
            maxLocalCalls,
            17,
            parallelThreads,
            false,
            rootCalibrationFolder,
            null,
            ImmutableList.of(globalCalibratorResult.getCalibratedScenarioFile()),
            new Calibrator.ParticleSwarmOptimizationGCPSOInitializer()
        ).calibrate(localCalibrationProblem);

        final Evaluator evaluator = new Evaluator();
        evaluator.setCalibrationFolder(localCalibratorResult.getRunFolder());
        evaluator.setScenarioSource(localCalibratorResult.getCalibratedScenarioFile());
        evaluator.run();
    }

    public GenericOptimization buildLocalCalibrationProblem(
        final double[] solution,
        final double range
    ) {
        final GenericOptimization genericOptimization =
            GenericOptimization.fromFile(calibrationFile);
        for (int i = 0; i < genericOptimization.getParameters().size(); i++) {
            final SimpleOptimizationParameter parameter =
                ((SimpleOptimizationParameter) genericOptimization.getParameters().get(i));
            final double optimalValue = parameter.computeNumericValue(solution[i]);
            parameter.setMaximum(optimalValue * (1d + range));
            parameter.setMinimum(optimalValue * (1d - range));
            if (parameter.getMaximum() == parameter.getMinimum()) {
                assert parameter.getMinimum() == 0;
                parameter.setMaximum(0.001);
            }
        }
        return genericOptimization;
    }

}
