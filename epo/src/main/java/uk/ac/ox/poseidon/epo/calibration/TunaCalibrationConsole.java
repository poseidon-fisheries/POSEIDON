/*
 * POSEIDON, an agent-based model of fisheries
 * Copyright (C) 2024 CoHESyS Lab cohesys.lab@gmail.com
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

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import static java.util.Arrays.stream;

public class TunaCalibrationConsole {

    @Parameter(
        names = "--nickname",
        description = "A nickname for the folder containing the outputs",
        required = false,
        arity = 1
    )
    private String runNickName = null;

    @Parameter(
        names = "--path",
        description = "Path to calibration.yaml",
        required = false,
        arity = 1
    )
    private String pathToCalibrationYaml = null;

    @Parameter(
        names = "--pop",
        description = "Size of optimizer population size"
    )
    private int populationSize = TunaCalibrator.DEFAULT_POPULATION_SIZE;

    @Parameter(
        names = "--maxProcs",
        description = "maximum number of processors"
    )
    private int maxProcessorsToUse = TunaCalibrator.MAX_PROCESSORS_TO_USE;

    @Parameter(
        names = "--runs",
        description = "maximum number of fitness calls"
    )
    private int maxFitnessCalls = TunaCalibrator.MAX_FITNESS_CALLS;

    @Parameter(
        names = "--range",
        description = "EVA range of parameters, by default this is 10"
    )
    private int parameterRange = TunaCalibrator.DEFAULT_RANGE;

    @Parameter(
        names = "--local",
        description = "when true this is a Nelder-Mead search"

    )
    private boolean isLocalSearch = false;

    @Parameter(
        names = "--pso",
        description = "when true this is a Particle Swarm search"

    )
    private boolean isPSO = false;

    @Parameter(
        names = "--initialGuessesTextFile",
        description = "path to text file where each line is an individual (comma separated double array) we need to " +
            "add in the first population"

    )
    private String bestGuessesTextFile = null;

    @Parameter(
        names = "--runsPerSetting",
        description = "overrides the number of simulations per parameter specified by the yaml file with argument"

    )
    private int numberOfRunsPerSettingOverride = -1;

    public static void main(final String[] args) throws IOException {
        // parse all arguments
        final TunaCalibrationConsole arguments = new TunaCalibrationConsole();
        final JCommander commander = JCommander.newBuilder().addObject(arguments).build();
        commander.parse(args);

        // build calibrator and feed it the arguments
        final TunaCalibrator tunaCalibrator = arguments.generateCalibratorProblem();
        // run the bastard
        final double[] solution = tunaCalibrator.run();

    }

    public TunaCalibrator generateCalibratorProblem() throws IOException {
        // build calibrator and feed it the arguments
        final TunaCalibrator tunaCalibrator = new TunaCalibrator();
        tunaCalibrator.setMaxFitnessCalls(this.getMaxFitnessCalls());
        tunaCalibrator.setPopulationSize(this.getPopulationSize());
        if (this.pathToCalibrationYaml != null && !this.pathToCalibrationYaml.trim().isEmpty())
            tunaCalibrator.setOriginalCalibrationFilePath(Paths.get(this.getPathToCalibrationYaml()));

        tunaCalibrator.setRunNickName(this.getRunNickName());
        tunaCalibrator.setParameterRange(this.getParameterRange());
        if (this.isLocalSearch())
            tunaCalibrator.setOptimizationRoutine(TunaCalibrator.OptimizationRoutine.NELDER_MEAD);
        if (this.isPSO)
            tunaCalibrator.setOptimizationRoutine(TunaCalibrator.OptimizationRoutine.PARTICLE_SWARM);
        tunaCalibrator.setMaxProcessorsToUse(this.getMaxProcessorsToUse());
        tunaCalibrator.setNumberOfRunsPerSettingOverride(this.getNumberOfRunsPerSettingOverride());

        // add initial guesses if provided
        if (this.getBestGuessesTextFile() != null && !this.getBestGuessesTextFile().trim().isEmpty()) {
            tunaCalibrator.getBestGuess().clear();
            final List<String> allLines = Files.readAllLines(Paths.get(this.getBestGuessesTextFile()));
            final List<double[]> individuals = new LinkedList<>();
            for (final String readLine : allLines) {
                if (readLine.trim().isEmpty())
                    continue;
                final double[] individual = stream(readLine.trim().split(","))
                    .mapToDouble(Double::parseDouble)
                    .toArray();
                individuals.add(individual);

            }
            tunaCalibrator.setBestGuess(individuals);

        }
        System.out.println(tunaCalibrator.getBestGuess());
        return tunaCalibrator;
    }

    public int getMaxFitnessCalls() {
        return maxFitnessCalls;
    }

    public void setMaxFitnessCalls(final int maxFitnessCalls) {
        this.maxFitnessCalls = maxFitnessCalls;
    }

    public int getPopulationSize() {
        return populationSize;
    }

    public void setPopulationSize(final int populationSize) {
        this.populationSize = populationSize;
    }

    public String getPathToCalibrationYaml() {
        return pathToCalibrationYaml;
    }

    public void setPathToCalibrationYaml(final String pathToCalibrationYaml) {
        this.pathToCalibrationYaml = pathToCalibrationYaml;
    }

    public String getRunNickName() {
        return runNickName;
    }

    public void setRunNickName(final String runNickName) {
        this.runNickName = runNickName;
    }

    public int getParameterRange() {
        return parameterRange;
    }

    public void setParameterRange(final int parameterRange) {
        this.parameterRange = parameterRange;
    }

    public boolean isLocalSearch() {
        return isLocalSearch;
    }

    public void setLocalSearch(final boolean localSearch) {
        isLocalSearch = localSearch;
    }

    public int getMaxProcessorsToUse() {
        return maxProcessorsToUse;
    }

    public void setMaxProcessorsToUse(final int maxProcessorsToUse) {
        this.maxProcessorsToUse = maxProcessorsToUse;
    }

    public int getNumberOfRunsPerSettingOverride() {
        return numberOfRunsPerSettingOverride;
    }

    public void setNumberOfRunsPerSettingOverride(final int numberOfRunsPerSettingOverride) {
        this.numberOfRunsPerSettingOverride = numberOfRunsPerSettingOverride;
    }

    public String getBestGuessesTextFile() {
        return bestGuessesTextFile;
    }

    public void setBestGuessesTextFile(final String bestGuessesTextFile) {
        this.bestGuessesTextFile = bestGuessesTextFile;
    }

    public boolean isPSO() {
        return isPSO;
    }

    public void setPSO(final boolean PSO) {
        isPSO = PSO;
    }
}
