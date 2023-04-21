package uk.ac.ox.oxfish.maximization;

import com.univocity.parsers.annotations.Parsed;
import uk.ac.ox.oxfish.maximization.generic.FixedDataTarget;
import uk.ac.ox.oxfish.maximization.generic.SimpleOptimizationParameter;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.Scenario;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.Streams.mapWithIndex;
import static com.google.common.collect.Streams.stream;
import static java.util.stream.LongStream.range;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.entry;
import static uk.ac.ox.oxfish.utility.csv.CsvParserUtil.writeBeans;

public class OneAtATimeSensitivity {

    private final GenericOptimization genericOptimization;
    private final double[] solution;

    public OneAtATimeSensitivity(
        final GenericOptimization genericOptimization,
        final double[] solution
    ) {
        this.genericOptimization = genericOptimization;
        this.solution = solution;
    }

    public static OneAtATimeSensitivity from(final Path calibrationFile, final Path logFile) {
        return new OneAtATimeSensitivity(
            GenericOptimization.fromFile(calibrationFile),
            new SolutionExtractor(logFile).bestSolution().getKey()
        );
    }

    public void run(
        final int steps,
        final int iterations,
        final int numYearsToRun,
        final Path outputFolder
    ) {
        final List<Variation> variations = buildVariations(steps).collect(toImmutableList());
        writeBeans(outputFolder.resolve("variations.csv"), variations, Variation.class);
        final Stream<Result> results = getResults(variations, iterations, numYearsToRun);
        writeBeans(outputFolder.resolve("results.csv"), results::iterator, Result.class);
    }

    public Stream<Variation> buildVariations(
        final int steps
    ) {
        return mapWithIndex(
            getParameters().stream().flatMap(parameter ->
                valueRange(baseScenario(), parameter, steps).mapToObj(value ->
                    entry(parameter, value)
                )
            ),
            (parameterAndValue, index) -> {
                final Scenario scenario = baseScenario();
                parameterAndValue.getKey().getSetter(scenario).accept(parameterAndValue.getValue());
                return new Variation(
                    index,
                    parameterAndValue.getKey().getAddressToModify(),
                    parameterAndValue.getValue(),
                    scenario
                );
            }
        );
    }

    public Stream<Result> getResults(
        final Iterable<Variation> variations,
        final int iterations,
        final int numYearsToRun
    ) {
        return stream(variations)
            .parallel()
            .flatMap(variation ->
                range(0, iterations).boxed()
                    .flatMap(iteration -> {
                        final FishState fishState = variation.run(numYearsToRun);
                        return getTargets().stream().map(t ->
                            new Result(
                                variation.variationId,
                                iteration,
                                t.getColumnName(),
                                t.getFixedTarget(),
                                t.getValue(fishState)
                            )
                        );
                    })
            );
    }

    public List<SimpleOptimizationParameter> getParameters() {
        return genericOptimization.getParameters()
            .stream()
            .filter(p -> p instanceof SimpleOptimizationParameter)
            .map(p -> (SimpleOptimizationParameter) p)
            .collect(toImmutableList());
    }

    public DoubleStream valueRange(
        final Scenario scenario,
        final SimpleOptimizationParameter parameter,
        final int steps
    ) {
        return valueRange(
            parameter.getValue(scenario),
            parameter.getMinimum(),
            parameter.getMaximum(),
            parameter.getMinimum(),
            parameter.getMaximum(),
            steps
        );
    }

    public Scenario baseScenario() {
        return genericOptimization.buildScenario(solution);
    }

    public List<FixedDataTarget> getTargets() {
        return genericOptimization.getTargets()
            .stream()
            .filter(t -> t instanceof FixedDataTarget)
            .map(t -> (FixedDataTarget) t)
            .collect(toImmutableList());
    }

    static DoubleStream valueRange(
        final double value,
        final double minimum,
        final double maximum,
        final double low,
        final double high,
        final int steps
    ) {
        final double start = Math.max(minimum, value * low);
        final double end = Math.min(maximum, value * high);
        final double delta = (end > 0 ? end : maximum) - start;
        return range(0, steps).mapToDouble(i -> start + delta * ((double) i / (steps - 1)));
    }

    public static class Variation {
        @Parsed
        private final long variationId;
        @Parsed
        private final String variedParameterAddress;
        @Parsed
        private final Double variedParameterValue;
        private final Scenario scenario;

        public Variation(
            final long id,
            final String variedParameterAddress,
            final Double variedParameterValue,
            final Scenario scenario
        ) {
            this.variationId = id;
            this.variedParameterAddress = variedParameterAddress;
            this.variedParameterValue = variedParameterValue;
            this.scenario = scenario;
        }

        public FishState run(final int numYearsToRun) {
            final FishState fishState = new FishState();
            fishState.setScenario(scenario);
            fishState.start();
            do {
                fishState.schedule.step(fishState);
            } while (fishState.getYear() < numYearsToRun);
            return fishState;
        }
    }

    public static class Result {
        @Parsed
        private final long variationId;
        @Parsed
        private final long iteration;
        @Parsed
        private final String columnName;
        @Parsed
        private final double target;
        @Parsed
        private final double value;
        @Parsed
        private final double error;

        public Result(
            final long variationId,
            final long iteration,
            final String columnName,
            final double target,
            final double value
        ) {
            this.variationId = variationId;
            this.iteration = iteration;
            this.columnName = columnName;
            this.target = target;
            this.value = value;
            this.error = value - target;
        }
    }

}
