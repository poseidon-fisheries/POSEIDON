package uk.ac.ox.oxfish.maximization;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.PathConverter;
import com.univocity.parsers.annotations.Parsed;
import uk.ac.ox.oxfish.maximization.generic.FixedDataTarget;
import uk.ac.ox.oxfish.maximization.generic.SimpleOptimizationParameter;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.Scenario;

import java.nio.file.Path;
import java.nio.file.Paths;
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

    @Parameter(names = {"-c", "--calibration_file"})
    private String calibrationFile = "calibration.yaml";
    @Parameter(names = {"-l", "--log_file"})
    private String logFile = "calibration_log.md";
    @Parameter(names = {"-f", "--folder"}, converter = PathConverter.class)
    private Path folder = Paths.get(".");
    @Parameter(names = {"-s", "--steps"})
    private int steps;
    @Parameter(names = {"-i", "--iterations"})
    private int iterations;
    @Parameter(names = {"-y", "--num_year_to_run"})
    private int numYearsToRun;

    @SuppressWarnings("WeakerAccess")
    public OneAtATimeSensitivity() {
    }

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
        final List<Variation> variations = buildVariations(genericOptimization).collect(toImmutableList());
        writeBeans(folder.resolve("variations.csv"), variations, Variation.class);
        final Stream<Result> results = getResults(genericOptimization, variations);
        writeBeans(folder.resolve("results.csv"), results::iterator, Result.class);
    }

    private Stream<Variation> buildVariations(
        final GenericOptimization genericOptimization
    ) {
        final double[] solution = new SolutionExtractor(folder.resolve(logFile)).bestSolution().getKey();

        return mapWithIndex(
            getParameters(genericOptimization).stream().flatMap(parameter ->
                valueRange(parameter, steps).mapToObj(value ->
                    entry(parameter, value)
                )
            ),
            (parameterAndValue, index) -> {
                final Scenario scenario = genericOptimization.buildScenario(solution);
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

    private Stream<Result> getResults(
        final GenericOptimization genericOptimization,
        final Iterable<Variation> variations
    ) {
        return stream(variations)
            .parallel()
            .flatMap(variation ->
                range(0, iterations)
                    .boxed()
                    .parallel()
                    .flatMap(iteration -> {
                        final FishState fishState = variation.run(numYearsToRun);
                        return getTargets(genericOptimization).stream().map(t ->
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
        final int steps
    ) {
        return valueRange(
            parameter.getMinimum(),
            parameter.getMaximum(),
            steps
        );
    }

    private List<FixedDataTarget> getTargets(final GenericOptimization genericOptimization) {
        return genericOptimization.getTargets()
            .stream()
            .filter(t -> t instanceof FixedDataTarget)
            .map(t -> (FixedDataTarget) t)
            .collect(toImmutableList());
    }

    static DoubleStream valueRange(
        final double minimum,
        final double maximum,
        final int steps
    ) {
        final double delta = maximum - minimum;
        return range(0, steps).mapToDouble(i -> minimum + delta * ((double) i / (steps - 1)));
    }

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
    public static class Variation {
        @Parsed
        private final long variationId;
        @Parsed
        private final String variedParameterAddress;
        @Parsed
        private final Double variedParameterValue;
        private final Scenario scenario;

        @SuppressWarnings("WeakerAccess")
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

    @SuppressWarnings({"FieldCanBeLocal", "unused"})
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

        @SuppressWarnings("WeakerAccess")
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
