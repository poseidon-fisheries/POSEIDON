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
import org.jetbrains.annotations.NotNull;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.data.monitors.loggers.RowProvider;
import uk.ac.ox.oxfish.model.data.monitors.loggers.TidyFisherYearlyData;
import uk.ac.ox.oxfish.model.data.monitors.loggers.TidyYearlyData;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.stream.IntStream.range;
import static uk.ac.ox.oxfish.model.data.monitors.loggers.RowProvider.writeRows;

public final class Runner<S extends Scenario> {

    private static final String YEARLY_DATA_FILENAME = "yearly_data.csv";

    ;
    private static final String FISHER_YEARLY_DATA_FILENAME = "fisher_yearly_data.csv";
    private static final String RUNS_FILENAME = "runs.csv";
    private final Class<S> scenarioClass;
    private final Path scenarioPath;
    private final Path outputPath;
    private final Multimap<Path, AlgorithmFactory<Iterable<? extends RowProvider>>> rowProviderFactories =
        HashMultimap.create();
    private CsvWriterSettings csvWriterSettings = new CsvWriterSettings();
    private Collection<Policy<? super S>> policies = ImmutableList.of(new Policy<>("Default", "", __ -> {}));
    private Consumer<State> beforeStartConsumer = __ -> {};
    private Consumer<State> afterStartConsumer = __ -> {};
    private Consumer<State> afterStepConsumer = __ -> {};
    private Consumer<State> afterRunConsumer = __ -> {};
    private State state;

    Runner(
        final Class<S> scenarioClass,
        final Path scenarioPath,
        final Path outputPath
    ) {
        this.scenarioClass = scenarioClass;
        this.scenarioPath = scenarioPath;
        this.outputPath = outputPath;
        rowProviderFactories.put(outputPath.resolve(RUNS_FILENAME), __ -> ImmutableList.of(state));
    }

    @SuppressWarnings("unused")
    public Runner<S> setCsvWriterSettings(final CsvWriterSettings csvWriterSettings) {
        this.csvWriterSettings = csvWriterSettings;
        return this;
    }

    @SuppressWarnings({"unused"})
    public Runner<S> setBeforeStartConsumer(final Consumer<State> beforeStartConsumer) {
        this.beforeStartConsumer = beforeStartConsumer;
        return this;
    }

    public Runner<S> setAfterStartConsumer(final Consumer<State> afterStartConsumer) {
        this.afterStartConsumer = afterStartConsumer;
        return this;
    }

    @SuppressWarnings("unused")
    public Runner<S> setAfterStepConsumer(final Consumer<State> afterStepConsumer) {
        this.afterStepConsumer = checkNotNull(afterStepConsumer);
        return this;
    }

    @SuppressWarnings("WeakerAccess")
    public Runner<S> setAfterRunConsumer(final Consumer<State> afterRunConsumer) {
        this.afterRunConsumer = checkNotNull(afterRunConsumer);
        return this;
    }

    @SuppressWarnings("SameParameterValue")
    void run(int numYearsToRun) { run(numYearsToRun, 1); }

    void run(int numYearsToRun, int numberOfRunsPerPolicy) {
        int numRuns = policies.size() * numberOfRunsPerPolicy;
        final AtomicInteger runNumber = new AtomicInteger(1);
        range(0, numberOfRunsPerPolicy).forEach(i ->
            policies.forEach(policy -> {
                System.out.printf("===\nRun %d / %s\n===\n", runNumber.get(), numRuns);
                state = startRun(policy, runNumber.get(), numRuns, numYearsToRun);
                beforeStartConsumer.accept(state);
                state.model.start();
                afterStartConsumer.accept(state);
                final Multimap<Path, RowProvider> rowProviders = makeRowProviders(state.model);
                do {
                    state.printStep();
                    state.model.schedule.step(state.model);
                    afterStepConsumer.accept(state);
                } while (state.model.getYear() < numYearsToRun);
                afterRunConsumer.accept(state);
                writeOutputs(runNumber.getAndIncrement(), rowProviders);
            })
        );
    }

    @NotNull
    private State startRun(
        final Policy<? super S> policy,
        final int runNumber,
        final int numRuns,
        final int numYearsToRun
    ) {
        final LocalDateTime startTime = LocalDateTime.now();
        final S scenario = readScenario(scenarioPath, scenarioClass);
        policy.getScenarioConsumer().accept(scenario);
        FishState fishState = new FishState();
        fishState.setScenario(scenario);
        return new State(scenario, policy, fishState, runNumber, numRuns, numYearsToRun, startTime);
    }

    private Multimap<Path, RowProvider> makeRowProviders(FishState fishState) {
        ImmutableMultimap.Builder<Path, RowProvider> rowProviders = ImmutableMultimap.builder();
        rowProviderFactories.forEach((path, factory) ->
            factory.apply(fishState).forEach(rowProvider -> {
                if (rowProvider instanceof Startable)
                    fishState.registerStartable((Startable) rowProvider);
                rowProviders.put(path, rowProvider);
            })
        );
        return rowProviders.build();
    }

    private void writeOutputs(final int runNumber, Multimap<Path, RowProvider> rowProviders) {
        rowProviders.asMap().forEach((outputPath, providers) -> {
            try (Writer fileWriter = new FileWriter(outputPath.toFile(), runNumber > 1)) {
                final CsvWriter csvWriter = new CsvWriter(new BufferedWriter(fileWriter), csvWriterSettings);
                writeRows(csvWriter, providers, runNumber, runNumber == 1);
            } catch (IOException e) {
                throw new IllegalStateException("Writing to " + outputPath + " failed.", e);
            }
        });
    }

    private S readScenario(final Path scenarioPath, Class<S> scenarioClass) {
        try (FileReader fileReader = new FileReader(scenarioPath.toFile())) {
            final FishYAML fishYAML = new FishYAML();
            final S scenario = fishYAML.loadAs(fileReader, scenarioClass);
            writeScenarioToOutputFolder(fishYAML, scenario);
            return scenario;
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("Can't find scenario file: " + scenarioPath, e);
        } catch (IOException e) {
            throw new IllegalStateException("Error while reading file: " + scenarioPath, e);
        }
    }

    private void writeScenarioToOutputFolder(FishYAML fishYAML, S scenario) {
        final File outputFile = outputPath.resolve("scenario.yaml").toFile();
        if (!outputFile.isFile()) {
            try (Writer writer = new FileWriter(outputFile)) {
                fishYAML.dump(scenario, writer);
            } catch (IOException e) {
                throw new IllegalStateException("Error while writing file: " + outputFile, e);
            }
        }
    }

    Runner<S> requestFisherYearlyData() {
        return registerRowProviders(FISHER_YEARLY_DATA_FILENAME, fishState ->
            fishState.getFishers().stream()
                .map(fisher -> new TidyFisherYearlyData(fisher.getYearlyData(), fisher.getTags().get(0)))
                .collect(toImmutableList())
        );
    }

    Runner<S> registerRowProviders(
        String fileName,
        AlgorithmFactory<Iterable<? extends RowProvider>> rowProvidersFactory
    ) {
        rowProviderFactories.put(outputPath.resolve(fileName), rowProvidersFactory);
        return this;
    }

    Runner<S> requestYearlyData() {
        return registerRowProvider(YEARLY_DATA_FILENAME, fishState ->
            new TidyYearlyData(fishState.getYearlyDataSet())
        );
    }

    Runner<S> registerRowProvider(
        @SuppressWarnings("SameParameterValue") String fileName,
        AlgorithmFactory<? extends RowProvider> rowProviderFactory
    ) {
        registerRowProviders(
            fileName,
            fishState -> ImmutableList.of(rowProviderFactory.apply(fishState))
        );
        return this;
    }

    public Runner<S> setPolicies(final Iterable<Policy<S>> policies) {
        this.policies = ImmutableList.copyOf(policies);
        return this;
    }

    class State implements RowProvider {

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

        public Policy<? super S> getPolicy() { return policy; }

        public FishState getModel() { return model; }

        public int getRunNumber() { return runNumber; }

        public int getNumRuns() { return numRuns; }

        public int getNumYearsToRun() { return numYearsToRun; }

        public LocalDateTime getStartTime() { return startTime; }

        void printStep() {
            System.out.printf(
                "---\nRun %" + runDigits + "d / %" + runDigits + "d, " +
                    "step %5d (year %" + yearDigits + "d / %" + yearDigits + "d, " +
                    "day %3d), policy: %s\n",
                runNumber,
                numRuns,
                model.getStep(),
                model.getYear() + 1,
                numYearsToRun,
                model.getDayOfTheYear(),
                policy.getName()
            );
        }

        @Override public List<String> getHeaders() { return ImmutableList.of("policy", "run_start", "run_end"); }

        @Override public Iterable<? extends Collection<?>> getRows() {
            return ImmutableList.of(ImmutableList.of(policy.getName(), startTime, LocalDateTime.now()));
        }

        public S getScenario() { return scenario; }

    }

}
