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
import com.google.common.collect.Multimap;
import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.data.monitors.loggers.RowProvider;
import uk.ac.ox.oxfish.model.data.monitors.loggers.TidyFisherYearlyData;
import uk.ac.ox.oxfish.model.data.monitors.loggers.TidyYearlyData;
import uk.ac.ox.oxfish.model.scenario.TunaScenario;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.IntStream.range;
import static uk.ac.ox.oxfish.model.data.monitors.loggers.RowProvider.writeRows;

public final class TunaRunner {

    private final TunaScenario scenario;
    private final Path outputPath;
    private final CsvWriterSettings csvWriterSettings;
    private final List<Startable> startables = newArrayList();
    private final Multimap<Path, RowProvider> registeredRowProviders = HashMultimap.create();
    private int runNumber;
    private Consumer<FishState> beforeStartConsumer = __ -> System.out.printf("===\nRun %d\n===\n", runNumber);
    private Consumer<FishState> afterStepConsumer = TunaRunner::printStep;
    private Consumer<FishState> afterRunConsumer = __ -> {};

    TunaRunner(
        final Path scenarioPath,
        final Path outputPath,
        final CsvWriterSettings csvWriterSettings
    ) {
        this(
            readScenario(scenarioPath),
            outputPath,
            csvWriterSettings
        );
    }

    private TunaRunner(
        final TunaScenario scenario,
        final Path outputPath,
        final CsvWriterSettings csvWriterSettings
    ) {
        this.scenario = scenario;
        this.outputPath = outputPath;
        this.csvWriterSettings = csvWriterSettings;
    }

    private static TunaScenario readScenario(final Path scenarioPath) {
        try (FileReader fileReader = new FileReader(scenarioPath.toFile())) {
            return new FishYAML().loadAs(fileReader, TunaScenario.class);
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("Can't find scenario file: " + scenarioPath, e);
        } catch (IOException e) {
            throw new IllegalStateException("Error while reading file: " + scenarioPath, e);
        }
    }

    @SuppressWarnings("WeakerAccess")
    static void printStep(FishState fishState) {
        System.out.printf(
            "%5d (year %d, day %3d)\n",
            fishState.getStep(),
            fishState.getYear(),
            fishState.getDayOfTheYear()
        );
    }

    public TunaRunner setBeforeStartConsumer(final Consumer<FishState> beforeStartConsumer) {
        this.beforeStartConsumer = beforeStartConsumer;
        return this;
    }

    public TunaRunner setAfterStepConsumer(final Consumer<FishState> afterStepConsumer) {
        this.afterStepConsumer = checkNotNull(afterStepConsumer);
        return this;
    }

    public TunaRunner setAfterRunConsumer(final Consumer<FishState> afterRunConsumer) {
        this.afterRunConsumer = checkNotNull(afterRunConsumer);
        return this;
    }

    void runUntilYear(int year) { runUntilYear(year, 1); }

    void runUntilYear(int year, int numberOfRuns) {
        range(0, numberOfRuns).forEach(runNumber -> {
            this.runNumber = runNumber;
            FishState fishState = new FishState();
            fishState.setScenario(scenario);
            startables.forEach(fishState::registerStartable);
            beforeStartConsumer.accept(fishState);
            fishState.start();
            do {
                fishState.schedule.step(fishState);
                afterStepConsumer.accept(fishState);
            } while (fishState.getYear() < year);
            afterRunConsumer.accept(fishState);

            // write outputs
            registeredRowProviders.asMap().forEach((outputPath, rowProviders) -> {
                try (Writer fileWriter = new FileWriter(outputPath.toFile(), runNumber > 0)) {
                    final CsvWriter csvWriter = new CsvWriter(new BufferedWriter(fileWriter), csvWriterSettings);
                    writeRows(csvWriter, rowProviders, runNumber);
                } catch (IOException e) {
                    throw new IllegalStateException("Writing to " + outputPath + " failed.", e);
                }
            });
        });
    }

    public TunaScenario getScenario() { return scenario; }

    TunaRunner registerFisherYearlyData(
        @SuppressWarnings("SameParameterValue") String fileName
    ) {
        return registerRowProviders(fileName, fishState ->
            fishState.getFishers().stream()
                .map(fisher -> new TidyFisherYearlyData(fisher.getYearlyData(), fisher.getTags().get(0)))
                .collect(toImmutableList())
        );
    }

    TunaRunner registerRowProviders(
        String fileName,
        Function<FishState, Iterable<? extends RowProvider>> makeRowProviders
    ) {
        final Path path = outputPath.resolve(fileName);
        startables.add(fishState ->
            makeRowProviders.apply(fishState).forEach(rowProvider -> {
                registeredRowProviders.put(path, rowProvider);
                if (rowProvider instanceof Startable)
                    ((Startable) rowProvider).start(fishState);
            })
        );
        return this;
    }

    TunaRunner registerYearlyData(
        @SuppressWarnings("SameParameterValue") String fileName
    ) {
        return registerRowProvider(fileName, fishState ->
            new TidyYearlyData(fishState.getYearlyDataSet())
        );
    }

    TunaRunner registerRowProvider(
        @SuppressWarnings("SameParameterValue") String fileName,
        Function<FishState, ? extends RowProvider> makeRowProviders
    ) {
        registerRowProviders(
            fileName,
            makeRowProviders.andThen(ImmutableList::of)
        );
        return this;
    }

}
