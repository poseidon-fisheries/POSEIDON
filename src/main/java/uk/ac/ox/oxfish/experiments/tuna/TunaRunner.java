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

import com.google.common.collect.ImmutableList;
import com.univocity.parsers.csv.CsvWriter;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.data.heatmaps.HeatmapGatherer;
import uk.ac.ox.oxfish.model.data.monitors.loggers.TidyFisherYearlyData;
import uk.ac.ox.oxfish.model.scenario.TunaScenario;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static uk.ac.ox.oxfish.model.data.monitors.loggers.RowsProvider.writeRows;

public final class TunaRunner {

    private final TunaScenario scenario;
    private final FishState model;
    private final Collection<HeatmapGatherer> heatmapGatherers = new ArrayList<>();

    TunaRunner(final Path scenarioPath) {
        this(readScenario(scenarioPath));
    }

    private TunaRunner(final TunaScenario scenario) {
        this.scenario = scenario;
        this.model = new FishState();
        this.model.setScenario(scenario);
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

    void runUntilYear(int year, Consumer<FishState> modelConsumer) {
        model.start();
        do {
            model.schedule.step(model);
            modelConsumer.accept(model);
        } while (model.getYear() < year);
    }

    public TunaScenario getScenario() { return scenario; }

    void registerHeatmapGatherers(
        Function<FishState, Iterable<HeatmapGatherer>> heatmapGathererProducer
    ) {
        registerStartables(heatmapGathererProducer, heatmapGatherers::add);
    }

    private <T extends Startable> void registerStartables(
        Function<FishState, Iterable<T>> startableProducer,
        Consumer<T> postStartConsumer
    ) {
        getModel().registerStartable(fishState ->
            startableProducer.apply(fishState).forEach(startable -> {
                startable.start(fishState);
                postStartConsumer.accept(startable);
            })
        );
    }

    public FishState getModel() { return model; }

    <T extends Startable> void registerStartable(
        Function<FishState, T> startableProducer,
        Consumer<T> postStartConsumer
    ) {
        registerStartables(startableProducer.andThen(ImmutableList::of), postStartConsumer);
    }

    void writeHeatmapData(final CsvWriter csvWriter) {
        writeRows(csvWriter, heatmapGatherers);
    }

    void writeFisherYearlyData(final CsvWriter csvWriter) {
        final List<TidyFisherYearlyData> fisherYearlyData =
            model.getFishers().stream()
                .map(fisher -> new TidyFisherYearlyData(fisher, fisher.getTags().get(0)))
                .collect(toImmutableList());
        writeRows(csvWriter, fisherYearlyData);
    }

}
