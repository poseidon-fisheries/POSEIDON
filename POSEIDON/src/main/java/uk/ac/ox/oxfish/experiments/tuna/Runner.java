/*
 *  POSEIDON, an agent-based model of fisheries
 *  Copyright (C) 2020  CoHESyS Lab cohesys.lab@gmail.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package uk.ac.ox.oxfish.experiments.tuna;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.data.monitors.loggers.RowProvider;
import uk.ac.ox.oxfish.model.data.monitors.loggers.TidyFisherDailyData;
import uk.ac.ox.oxfish.model.data.monitors.loggers.TidyFisherYearlyData;
import uk.ac.ox.oxfish.model.data.monitors.loggers.TidyYearlyData;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.stream.IntStream.range;
import static uk.ac.ox.oxfish.model.data.monitors.loggers.RowProvider.writeRows;

public final class Runner<S extends Scenario> {

    private static final Logger logger = Logger.getLogger(Runner.class.getName());
    private static final String YEARLY_DATA_FILENAME = "yearly_data.csv";
    private static final String FISHER_YEARLY_DATA_FILENAME = "fisher_yearly_data.csv";
    private static final String FISHER_DAILY_DATA_FILENAME = "fisher_daily_data.csv";
    private static final String RUNS_FILENAME = "runs.csv";
    private static final String POLICIES_FILENAME = "policies.csv";
    private static final String SCENARIOS_FOLDER = "scenarios";

    private final Map<Path, AtomicBoolean> overwriteFiles = new HashMap<>();

    private final Supplier<? extends S> scenarioSupplier;
    private final Path outputPath;
    private final Multimap<Path, AlgorithmFactory<Iterable<? extends RowProvider>>> rowProviderFactories =
        HashMultimap.create();
    private boolean parallel = true;
    private boolean writeScenarioToFile = false;
    private CsvWriterSettings csvWriterSettings = new CsvWriterSettings();
    private Collection<Policy<? super S>> policies = ImmutableList.of(Policy.DEFAULT);
    private Consumer<? super State> beforeStartConsumer = __ -> {};
    private Consumer<? super State> afterStartConsumer = __ -> {};
    private Consumer<State> afterStepConsumer = __ -> {};
    private Consumer<State> afterRunConsumer = __ -> {};

    public Runner(
        final Supplier<? extends S> scenarioSupplier,
        final Path outputPath
    ) {
        this.scenarioSupplier = scenarioSupplier;
        this.outputPath = outputPath;
    }

    @SuppressWarnings("unused")
    public Runner(
        final Class<S> scenarioClass,
        final Path scenarioPath
    ) {
        this(scenarioClass, scenarioPath, scenarioPath.getParent());
    }

    public Runner(
        final Class<S> scenarioClass,
        final Path scenarioPath,
        final Path outputPath
    ) {
        this.scenarioSupplier = makeScenarioSupplier(scenarioPath, scenarioClass);
        this.outputPath = outputPath;
    }

    private static <S extends Scenario> Supplier<S> makeScenarioSupplier(
        final Path scenarioPath,
        final Class<S> scenarioClass
    ) {
        return () -> {
            try (final FileReader fileReader = new FileReader(scenarioPath.toFile())) {
                final FishYAML fishYAML = new FishYAML();
                return fishYAML.loadAs(fileReader, scenarioClass);
            } catch (final FileNotFoundException e) {
                throw new IllegalArgumentException("Can't find scenario file: " + scenarioPath, e);
            } catch (final IOException e) {
                throw new IllegalStateException("Error while reading file: " + scenarioPath, e);
            }
        };
    }

    @SuppressWarnings("unused")
    public Path getOutputPath() {
        return outputPath;
    }

    @SuppressWarnings("unused")
    public boolean isParallel() {
        return parallel;
    }

    @SuppressWarnings("unused")
    public Runner<S> setParallel(final boolean parallel) {
        this.parallel = parallel;
        return this;
    }

    @SuppressWarnings("unused")
    public Runner<S> setCsvWriterSettings(final CsvWriterSettings csvWriterSettings) {
        this.csvWriterSettings = csvWriterSettings;
        return this;
    }

    @SuppressWarnings({"unused", "UnusedReturnValue"})
    public Runner<S> setBeforeStartConsumer(final Consumer<? super State> beforeStartConsumer) {
        this.beforeStartConsumer = beforeStartConsumer;
        return this;
    }

    @SuppressWarnings("WeakerAccess")
    public Runner<S> setAfterStartConsumer(final Consumer<? super State> afterStartConsumer) {
        this.afterStartConsumer = afterStartConsumer;
        return this;
    }

    @SuppressWarnings("unused")
    public Runner<S> setAfterStepConsumer(final Consumer<State> afterStepConsumer) {
        this.afterStepConsumer = checkNotNull(afterStepConsumer);
        return this;
    }

    @SuppressWarnings({"WeakerAccess", "unused"})
    public Runner<S> setAfterRunConsumer(final Consumer<State> afterRunConsumer) {
        this.afterRunConsumer = checkNotNull(afterRunConsumer);
        return this;
    }

    @SuppressWarnings("unused")
    public boolean isWriteScenarioToFile() {
        return writeScenarioToFile;
    }

    @SuppressWarnings("unused")
    public Runner<S> setWriteScenarioToFile(final boolean writeScenarioToFile) {
        this.writeScenarioToFile = writeScenarioToFile;
        return this;
    }

    @SuppressWarnings("SameParameterValue")
    public void run(final int numYearsToRun) {
        run(numYearsToRun, 1);
    }

    public void run(
        final int numYearsToRun,
        final int numberOfRunsPerPolicy
    ) {
        run(numYearsToRun, numberOfRunsPerPolicy, new AtomicInteger(1));
    }

    public void run(
        final int numYearsToRun,
        final int numberOfRunsPerPolicy,
        final AtomicInteger runCounter
    ) {
        final int numRuns = policies.size() * numberOfRunsPerPolicy;
        final IntStream range = range(0, numberOfRunsPerPolicy);
        (parallel ? range.parallel() : range).forEach(i -> (parallel ? policies.parallelStream()
            : policies.stream()).forEach(policy -> {
            final int runNumber = runCounter.getAndIncrement();
            logger.info(String.format("=== Starting run %d / %s ===", runNumber, numRuns));
            final State state = startRun(policy, runNumber, numRuns, numYearsToRun);
            if (writeScenarioToFile) writeScenarioToFile(state);
            beforeStartConsumer.accept(state);
            state.model.start();
            afterStartConsumer.accept(state);
            final Multimap<Path, RowProvider> rowProviders = makeRowProviders(state);
            do {
                writeOutputs(runNumber, rowProviders, false);
                state.printStep();
                state.model.schedule.step(state.model);
                afterStepConsumer.accept(state);
            } while (state.model.getYear() < numYearsToRun);
            afterRunConsumer.accept(state);
            writeOutputs(runNumber, rowProviders, true);
        }));
    }

    private State startRun(
        final Policy<? super S> policy,
        final int runNumber,
        final int numRuns,
        final int numYearsToRun
    ) {
        final LocalDateTime startTime = LocalDateTime.now();
        final S scenario = scenarioSupplier.get();
        policy.getScenarioConsumer().accept(scenario);
        final FishState fishState = new FishState(System.currentTimeMillis() + runNumber);
        fishState.setScenario(scenario);
        return new State(scenario, policy, fishState, runNumber, numRuns, numYearsToRun, startTime);
    }

    private void writeScenarioToFile(final State runnerState) {
        try {
            final Path scenariosFolder = outputPath.resolve(SCENARIOS_FOLDER);
            Files.createDirectories(scenariosFolder);
            final Path scenarioFile =
                scenariosFolder.resolve(runnerState.getPolicy().getName().replaceAll("[^a-zA-Z0-9-_.]", "_") + ".yaml");
            new FishYAML().dump(runnerState.getScenario(), new FileWriter(scenarioFile.toFile()));
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Multimap<Path, RowProvider> makeRowProviders(final State state) {
        final ImmutableMultimap.Builder<Path, RowProvider> rowProviders = ImmutableMultimap.builder();
        rowProviders.put(outputPath.resolve(RUNS_FILENAME), state);
        rowProviders.put(outputPath.resolve(POLICIES_FILENAME), state.policy);
        rowProviderFactories.forEach((path, factory) -> factory.apply(state.model).forEach(rowProvider -> {
            if (rowProvider instanceof Startable) {
                state.model.registerStartable((Startable) rowProvider);
            }
            rowProviders.put(path, rowProvider);
        }));
        return rowProviders.build();
    }

    private void writeOutputs(
        final int runNumber,
        final Multimap<? extends Path, RowProvider> rowProviders,
        final boolean isFinalStep
    ) {
        rowProviders.asMap().forEach((outputPath, providers) -> {
            // noinspection ResultOfMethodCallIgnored
            outputPath.getParent().toFile().mkdirs();
            final Collection<RowProvider> activeProviders = isFinalStep ? providers
                : providers.stream().filter(RowProvider::isEveryStep).collect(toImmutableList());
            if (!activeProviders.isEmpty()) {
                synchronized (overwriteFiles) {
                    final boolean overwrite =
                        overwriteFiles.computeIfAbsent(outputPath, __ -> new AtomicBoolean(true)).getAndSet(false);
                    try (final Writer fileWriter = new FileWriter(outputPath.toFile(), !overwrite)) {
                        final CsvWriter csvWriter = new CsvWriter(new BufferedWriter(fileWriter), csvWriterSettings);
                        writeRows(csvWriter, activeProviders, runNumber, overwrite);
                    } catch (final IOException e) {
                        throw new IllegalStateException("Writing to " + outputPath + " failed.", e);
                    }
                }
            }
        });
    }

    public Runner<S> writeScenarioToFile(final String outputFileName) {
        final File outputFile = outputPath.resolve(outputFileName).toFile();
        try (final Writer writer = new FileWriter(outputFile)) {
            new FishYAML().dump(scenarioSupplier.get(), writer);
        } catch (final IOException e) {
            throw new IllegalStateException("Error while writing file: " + outputFile, e);
        }
        return this;
    }

    public Runner<S> requestFisherYearlyData() {
        return requestFisherYearlyData(__ -> true);
    }

    public Runner<S> requestFisherYearlyData(final Predicate<String> columnNamePredicate) {
        return registerRowProviders(
            FISHER_YEARLY_DATA_FILENAME,
            fishState -> fishState
                .getFishers()
                .stream()
                .map(fisher -> new TidyFisherYearlyData(
                    fisher.getYearlyData(),
                    columnNamePredicate,
                    fisher.getTagsList().get(0)
                ))
                .collect(toImmutableList())
        );
    }

    @SuppressWarnings("WeakerAccess")
    Runner<S> registerRowProviders(
        final String fileName,
        final AlgorithmFactory<Iterable<? extends RowProvider>> rowProvidersFactory
    ) {
        rowProviderFactories.put(outputPath.resolve(fileName), rowProvidersFactory);
        return this;
    }

    public Runner<S> requestFisherDailyData(final Predicate<String> columnNamePredicate) {
        return registerRowProviders(
            FISHER_DAILY_DATA_FILENAME,
            fishState -> fishState
                .getFishers()
                .stream()
                .map(fisher -> new TidyFisherDailyData(
                    fisher.getDailyData(),
                    columnNamePredicate,
                    fisher.getTagsList().get(0)
                ))
                .collect(toImmutableList())
        );
    }

    @SuppressWarnings({"WeakerAccess", "unused"})
    public Runner<S> requestYearlyData(final Predicate<String> columnNamePredicate) {
        return registerRowProvider(YEARLY_DATA_FILENAME, fishState -> new TidyYearlyData(
            fishState.getYearlyDataSet(),
            columnNamePredicate
        ));
    }

    public Runner<S> registerRowProvider(
        @SuppressWarnings("SameParameterValue") final String fileName,
        final AlgorithmFactory<? extends RowProvider> rowProviderFactory
    ) {
        registerRowProviders(fileName, fishState -> ImmutableList.of(rowProviderFactory.apply(fishState)));
        return this;
    }

    public Runner<S> setPolicies(final Iterable<? extends Policy<? super S>> policies) {
        this.policies = ImmutableList.copyOf(policies);
        return this;
    }

    public class State implements RowProvider {

        private final S scenario;
        private final Policy<? super S> policy;
        private final FishState model;
        private final int runNumber;
        private final int numRuns;
        private final int numYearsToRun;
        private final LocalDateTime startTime;
        private final int runDigits;
        private final int yearDigits;

        State(
            final S scenario,
            final Policy<? super S> policy,
            final FishState model,
            final int runNumber,
            final int numRuns,
            final int numYearsToRun,
            final LocalDateTime startTime
        ) {
            this.scenario = scenario;
            this.policy = policy;
            this.model = model;
            this.runNumber = runNumber;
            this.numRuns = numRuns;
            this.numYearsToRun = numYearsToRun;
            this.startTime = startTime;
            runDigits = (int) (Math.log10(numRuns) + 1);
            yearDigits = (int) (Math.log10(numYearsToRun) + 1);
        }

        public Policy<? super S> getPolicy() {
            return policy;
        }

        public FishState getModel() {
            return model;
        }

        @SuppressWarnings("unused")
        public int getRunNumber() {
            return runNumber;
        }

        @SuppressWarnings("unused")
        public int getNumRuns() {
            return numRuns;
        }

        @SuppressWarnings("unused")
        public int getNumYearsToRun() {
            return numYearsToRun;
        }

        @SuppressWarnings("unused")
        public LocalDateTime getStartTime() {
            return startTime;
        }

        void printStep() {
            logger.info(String.format(
                "Run %" + runDigits + "d / %" + runDigits + "d, " + "step %5d (year %" + yearDigits + "d / %" +
                    yearDigits + "d, " + "day %3d), policy: %s",
                runNumber,
                numRuns,
                model.getStep(),
                model.getYear() + 1,
                numYearsToRun,
                model.getDayOfTheYear(),
                policy.getName()
            ));
        }

        @Override
        public List<String> getHeaders() {
            return ImmutableList.of("run_start", "run_end");
        }

        @Override
        public Iterable<? extends Collection<?>> getRows() {
            return ImmutableList.of(ImmutableList.of(startTime, LocalDateTime.now()));
        }

        public S getScenario() {
            return scenario;
        }

    }

}
