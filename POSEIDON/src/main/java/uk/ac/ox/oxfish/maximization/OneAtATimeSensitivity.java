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
package uk.ac.ox.oxfish.maximization;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.PathConverter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import com.univocity.parsers.annotations.Parsed;
import uk.ac.ox.oxfish.experiments.tuna.Policy;
import uk.ac.ox.oxfish.experiments.tuna.Runner;
import uk.ac.ox.oxfish.maximization.generic.FixedDataTarget;
import uk.ac.ox.oxfish.maximization.generic.SimpleOptimizationParameter;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.monitors.loggers.RowProvider;
import uk.ac.ox.oxfish.model.scenario.Scenario;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.DoubleStream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.stream.LongStream.range;

public class OneAtATimeSensitivity {

    @Parameter(names = {"-c", "--calibration_file"})
    private String calibrationFile = "calibration.yaml";
    @Parameter(names = {"-l", "--log_file"})
    private String logFile = "calibration_log.md";
    @Parameter(names = {"-f", "--folder"}, converter = PathConverter.class)
    private Path folder = Paths.get(".");
    @Parameter(names = {"-s", "--steps"})
    private int steps = 20;
    @Parameter(names = {"-i", "--iterations"})
    private int iterations = 5;
    @Parameter(names = {"-y", "--num_year_to_run"})
    private int numYearsToRun = 2;
    @Parameter(names = {"-b", "--bounds_only"})
    private boolean boundsOnly = false;

    @SuppressWarnings("WeakerAccess")
    public OneAtATimeSensitivity() {
    }

    @SuppressWarnings("unused")
    public OneAtATimeSensitivity(
        final String calibrationFile,
        final String logFile,
        final Path folder,
        final int steps,
        final int iterations,
        final int numYearsToRun
    ) {
        this.calibrationFile = calibrationFile;
        this.logFile = logFile;
        this.folder = folder;
        this.steps = steps;
        this.iterations = iterations;
        this.numYearsToRun = numYearsToRun;
    }

    public static void main(final String[] args) {
        final OneAtATimeSensitivity oneAtATimeSensitivity = new OneAtATimeSensitivity();
        JCommander.newBuilder()
            .addObject(oneAtATimeSensitivity)
            .build()
            .parse(args);
        oneAtATimeSensitivity.run();
    }

    public void run() {
        final GenericOptimization genericOptimization = GenericOptimization.fromFile(folder.resolve(calibrationFile));
        final double[] solution = new SolutionExtractor(folder.resolve(logFile)).bestSolution().getKey();
        final Path outputFolder = folder.resolve("ofat_outputs");
        if (!boundsOnly) {
            final Scenario scenario = genericOptimization.buildScenario(solution);
            new Runner<>(() -> genericOptimization.buildScenario(solution), outputFolder)
                .setPolicies(buildVariations(genericOptimization, scenario))
                .registerRowProvider(
                    "results.csv",
                    fishState -> new ResultsProvider(genericOptimization, fishState)
                )
                .run(numYearsToRun, iterations);
        }
    }

    private List<Variation> buildVariations(
        final GenericOptimization genericOptimization,
        final Scenario optimizedScenario
    ) {

        return getParameters(genericOptimization)
            .stream()
            .flatMap(parameter ->
                valueRange(parameter, parameter.getValue(optimizedScenario), steps).mapToObj(value ->
                    new Variation(
                        parameter.getAddressToModify(),
                        value,
                        scenario -> parameter.getSetter(scenario).accept(value)
                    )
                )
            ).collect(toImmutableList());
    }

    public List<SimpleOptimizationParameter> getParameters(
        final GenericOptimization genericOptimization
    ) {
        return genericOptimization.getParameters()
            .stream()
            .filter(p -> p instanceof SimpleOptimizationParameter)
            .map(p -> (SimpleOptimizationParameter) p)
            .collect(toImmutableList());
    }

    private DoubleStream valueRange(
        final SimpleOptimizationParameter parameter,
        final double parameterValue,
        final int steps
    ) {
        return valueRange(
            min(parameterValue, parameter.getMinimum()),
            max(parameterValue, parameter.getMaximum()),
            steps
        );
    }

    static DoubleStream valueRange(
        final double minimum,
        final double maximum,
        final int steps
    ) {
        final double delta = maximum - minimum;
        return Streams.concat(
            range(0, steps - 1).mapToDouble(i -> minimum + delta * ((double) i / (steps - 1))),
            // We want to make sure we get the precise maximum and not a generated floating point
            // value that could be off by a tiny bit, so we add it to stream manually
            DoubleStream.of(maximum)
        );
    }

    @SuppressWarnings("unused")
    public boolean isBoundsOnly() {
        return boundsOnly;
    }

    @SuppressWarnings("unused")
    public void setBoundsOnly(final boolean boundsOnly) {
        this.boundsOnly = boundsOnly;
    }

    private List<FixedDataTarget> getTargets(final GenericOptimization genericOptimization) {
        return genericOptimization.getTargets()
            .stream()
            .filter(t -> t instanceof FixedDataTarget)
            .map(t -> (FixedDataTarget) t)
            .collect(toImmutableList());
    }

    @SuppressWarnings("unused")
    public Path getFolder() {
        return folder;
    }

    public int getSteps() {
        return steps;
    }

    public void setSteps(final int steps) {
        this.steps = steps;
    }

    @SuppressWarnings("unused")
    public int getIterations() {
        return iterations;
    }

    @SuppressWarnings("unused")
    public void setIterations(final int iterations) {
        this.iterations = iterations;
    }

    @SuppressWarnings("unused")
    public int getNumYearsToRun() {
        return numYearsToRun;
    }

    @SuppressWarnings("unused")
    public void setNumYearsToRun(final int numYearsToRun) {
        this.numYearsToRun = numYearsToRun;
    }

    @SuppressWarnings("unused")
    public String getCalibrationFile() {
        return calibrationFile;
    }

    @SuppressWarnings("unused")
    public void setCalibrationFile(final String calibrationFile) {
        this.calibrationFile = calibrationFile;
    }

    @SuppressWarnings("unused")
    public String getLogFile() {
        return logFile;
    }

    @SuppressWarnings("unused")
    public void setLogFile(final String logFile) {
        this.logFile = logFile;
    }

    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    public static class Variation extends Policy<Scenario> {
        @Parsed
        private final Double variedParameterValue;

        @SuppressWarnings("WeakerAccess")
        public Variation(
            final String variedParameterAddress,
            final Double variedParameterValue,
            final Consumer<Scenario> scenarioConsumer
        ) {
            super(variedParameterAddress, scenarioConsumer);
            this.variedParameterValue = variedParameterValue;
        }

        @Override
        public List<String> getHeaders() {
            return ImmutableList.<String>builder()
                .addAll(super.getHeaders())
                .add("parameter_value")
                .build();
        }

        @Override
        public Iterable<? extends List<?>> getRows() {
            return Streams.stream(super.getRows()).map(row ->
                ImmutableList.builder()
                    .addAll(row)
                    .add(variedParameterValue)
                    .build()
            ).collect(toImmutableList());
        }
    }

    class ResultsProvider implements RowProvider {
        private final GenericOptimization genericOptimization;
        private final FishState fishState;

        ResultsProvider(
            final GenericOptimization genericOptimization,
            final FishState fishState
        ) {
            this.genericOptimization = genericOptimization;
            this.fishState = fishState;
        }

        @Override
        public List<String> getHeaders() {
            return ImmutableList.of("column_name", "target", "value");
        }

        @Override
        public Iterable<? extends List<?>> getRows() {
            return getTargets(genericOptimization).stream().map(t ->
                ImmutableList.of(
                    t.getColumnName(),
                    t.getFixedTarget(),
                    t.getValue(fishState)
                )
            ).collect(toImmutableList());
        }

    }

}
