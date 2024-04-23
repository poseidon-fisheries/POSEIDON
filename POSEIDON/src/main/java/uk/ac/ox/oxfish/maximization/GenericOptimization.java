/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2018-2024 CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.maximization;

import eva2.problems.simple.SimpleProblemDouble;
import uk.ac.ox.oxfish.maximization.generic.DataTarget;
import uk.ac.ox.oxfish.maximization.generic.OptimizationParameter;
import uk.ac.ox.oxfish.maximization.generic.SimpleOptimizationParameter;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;
import uk.ac.ox.poseidon.common.core.yaml.YamlLoader;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import static org.apache.commons.lang3.time.DurationFormatUtils.formatDuration;

public class GenericOptimization extends SimpleProblemDouble implements Serializable {

    private static final Logger logger = Logger.getLogger(GenericOptimization.class.getName());
    private static final long serialVersionUID = 3621186016008983379L;
    private String scenarioFile;

    private double translateNANto = 10000000;

    private boolean maximization = false;

    /**
     * list of all parameters that can be changed
     */
    private List<? extends OptimizationParameter> parameters = new LinkedList<>();
    /**
     * map linking the name of the YearlyDataSet in the model with the path to file containing the real time series
     */
    private List<DataTarget> targets = new LinkedList<>();
    private int runsPerSetting = 1;
    private int simulatedYears = 4;

    public GenericOptimization() {
    }

    public GenericOptimization(
        final String scenarioFile,
        final List<? extends OptimizationParameter> parameters,
        final List<DataTarget> targets,
        final int runsPerSetting,
        final int simulatedYears
    ) {
        this.scenarioFile = scenarioFile;
        this.parameters = parameters;
        this.targets = targets;
        this.runsPerSetting = runsPerSetting;
        this.simulatedYears = simulatedYears;
    }

    /**
     * create smaller optimization problem trying to climb within a small range of previously found optimal parameters
     * this assumes however all parameters are simple
     */
    @SuppressWarnings("SameParameterValue")
    public static void buildLocalCalibrationProblem(
        final Path optimizationFile,
        final double[] originalParameters,
        final String newCalibrationName,
        final double range
    ) throws IOException {
        final FishYAML yaml = new FishYAML();
        final GenericOptimization optimization = yaml.loadAs(
            new FileReader(optimizationFile.toFile()),
            GenericOptimization.class
        );
        for (int i = 0; i < optimization.getParameters().size(); i++) {
            final SimpleOptimizationParameter parameter = ((SimpleOptimizationParameter) optimization.getParameters()
                .get(i));
            final double optimalValue = parameter.computeNumericValue(originalParameters[i]);
            parameter.setMaximum(optimalValue * (1d + range));
            parameter.setMinimum(optimalValue * (1d - range));
            if (parameter.getMaximum() == parameter.getMinimum()) {
                assert parameter.getMinimum() == 0;
                parameter.setMaximum(0.001);
            }

        }
        yaml.dump(optimization, new FileWriter(optimizationFile.getParent().resolve(newCalibrationName).toFile()));
    }

    public List<? extends OptimizationParameter> getParameters() {
        return parameters;
    }

    public void setParameters(final List<? extends OptimizationParameter> parameters) {
        this.parameters = parameters;
    }

    public static GenericOptimization fromFile(final Path calibrationFile) {
        return new YamlLoader<>(GenericOptimization.class).load(calibrationFile);
    }

    Scenario buildScenario(final double[] solution) {
        try {
            return buildScenario(solution, Paths.get(getScenarioFile()).toFile(), getParameters());
        } catch (final FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static Scenario buildScenario(
        final double[] x,
        final File scenarioFile,
        final Iterable<? extends OptimizationParameter> parameterList
    )
        throws FileNotFoundException {
        final FishYAML yaml = new FishYAML();
        final Scenario scenario = yaml.loadAs(new
            FileReader(scenarioFile), Scenario.class);
        int parameter = 0;
        for (final OptimizationParameter optimizationParameter : parameterList) {
            optimizationParameter.parametrize(
                scenario,
                Arrays.copyOfRange(x, parameter,
                    parameter + optimizationParameter.size()
                )
            );
            parameter += optimizationParameter.size();
        }
        return scenario;
    }

    public String getScenarioFile() {
        return scenarioFile;
    }

    public void setScenarioFile(final String scenarioFile) {
        this.scenarioFile = scenarioFile;
    }

    /**
     * Return the problem dimension.
     *
     * @return the problem dimension
     */
    @Override
    public int getProblemDimension() {
        int sum = 0;
        for (final OptimizationParameter parameter : parameters)
            sum += parameter.size();
        return sum;
    }

    /**
     * Evaluate a double vector representing a possible problem solution as part of an individual in the EvA framework.
     * This makes up the target function to be evaluated.
     *
     * @param x a double vector to be evaluated
     * @return the fitness vector assigned to x as to the target function
     */
    @SuppressWarnings("CallToPrintStackTrace")
    @Override
    public double[] evaluate(final double[] x) {

        // read in and modify parameters
        final Instant start = Instant.now();
        final Scenario scenario;
        try {
            scenario = buildScenario(x, Paths.get(scenarioFile).toFile(), parameters);
        } catch (final FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        double error = 0;
        for (int i = 0; i < runsPerSetting; i++) {
            try {
                // run the model
                error += computeErrorGivenScenario(scenario, simulatedYears);
            } catch (final Exception e) {
                e.printStackTrace();
                error += translateNANto;
            }
        }
        final Instant finish = Instant.now();

        double finalError = error / (double) runsPerSetting;
        if (maximization)
            finalError = finalError * (-1);

        if (!Double.isFinite(finalError)) {
            finalError = translateNANto;
        }

        logger.info(String.format(
            "%n  error: %.2f, runs: %d, duration: %s%n  solution: %s",
            finalError,
            runsPerSetting,
            formatDuration(Duration.between(start, finish).toMillis(), "HH:mm:ss"),
            Arrays.toString(Arrays.stream(x).mapToObj(v -> String.format("%.2f", v)).toArray())
        ));
        return new double[]{finalError};
    }

    private double computeErrorGivenScenario(
        final Scenario scenario,
        final int simulatedYears
    ) {
        final FishState model = new FishState(System.currentTimeMillis());
        model.setScenario(scenario);
        model.start();
        while (model.getYear() < simulatedYears) {
            model.schedule.step(model);
        }
        model.schedule.step(model);
        return targets.stream().mapToDouble(t -> t.computeError(model)).sum();
    }

    @SuppressWarnings("WeakerAccess")
    public List<DataTarget> getTargets() {
        return targets;
    }

    @SuppressWarnings("unused")
    public void setTargets(final List<DataTarget> targets) {
        this.targets = targets;
    }

    @SuppressWarnings("unused")
    public int getRunsPerSetting() {
        return runsPerSetting;
    }

    @SuppressWarnings("WeakerAccess")
    public void setRunsPerSetting(final int runsPerSetting) {
        this.runsPerSetting = runsPerSetting;
    }

    /**
     * Getter for property 'simulatedYears'.
     *
     * @return Value for property 'simulatedYears'.
     */
    @SuppressWarnings("unused")
    public int getSimulatedYears() {
        return simulatedYears;
    }

    /**
     * Setter for property 'simulatedYears'.
     *
     * @param simulatedYears Value to set for property 'simulatedYears'.
     */
    @SuppressWarnings("unused")
    public void setSimulatedYears(final int simulatedYears) {
        this.simulatedYears = simulatedYears;
    }

    /**
     * Getter for property 'maximization'.
     *
     * @return Value for property 'maximization'.
     */
    public boolean isMaximization() {
        return maximization;
    }

    /**
     * Setter for property 'maximization'.
     *
     * @param maximization Value to set for property 'maximization'.
     */
    public void setMaximization(final boolean maximization) {
        this.maximization = maximization;
    }

    /**
     * Getter for property 'translateNANto'.
     *
     * @return Value for property 'translateNANto'.
     */
    @SuppressWarnings("unused")
    public double getTranslateNANto() {
        return translateNANto;
    }

    /**
     * Setter for property 'translateNANto'.
     *
     * @param translateNANto Value to set for property 'translateNANto'.
     */
    @SuppressWarnings("unused")
    public void setTranslateNANto(final double translateNANto) {
        this.translateNANto = translateNANto;
    }

}
