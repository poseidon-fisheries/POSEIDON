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
import com.vividsolutions.jts.geom.Coordinate;
import sim.field.grid.DoubleGrid2D;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.heatmaps.HeatmapGatherer;
import uk.ac.ox.oxfish.model.scenario.TunaScenario;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.google.common.collect.Streams.mapWithIndex;
import static java.lang.Math.toIntExact;
import static java.util.stream.IntStream.range;

public final class TunaRunner {

    private final TunaScenario scenario;
    private final FishState model;
    private List<HeatmapGatherer> heatmapGatherers = new ArrayList<>();

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

    @SuppressWarnings("UnstableApiUsage") Stream<List<?>> getFisherYearlyData(
        Function<Fisher, String> fisherIdExtractor
    ) {
        return model.getFishers().stream().flatMap(fisher ->
            fisher.getYearlyData().getColumns().stream().flatMap(column ->
                mapWithIndex(column.stream(), (value, index) -> ImmutableList.of(
                    fisherIdExtractor.apply(fisher), // boat_id
                    index + 1, // year
                    column.getName(), // variable
                    column.get(toIntExact(index)) // value
                ))
            )
        );
    }

    void writeHeatmapData(CsvWriter csvWriter) {
        csvWriter.writeHeaders("name", "step", "lon", "lat", "value", "unit");
        csvWriter.writeRowsAndClose(heatmapDataToRows()::iterator);
    }

    /**
     * Extract data fom a heatmap builder in a form suitable for writing in a tidy csv file
     * (i.e., as a stream of "rows")
     */
    private Stream<List<?>> heatmapDataToRows() {
        return heatmapGatherers.stream().flatMap(heatmapGatherer ->
            heatmapGatherer.getGrids().entrySet().stream().flatMap(entry -> {
                final Integer step = entry.getKey();
                final DoubleGrid2D grid = entry.getValue();
                return range(0, grid.getWidth()).boxed().flatMap(x ->
                    range(0, grid.getHeight()).mapToObj(y -> {
                        final Coordinate coordinates = model.getMap().getCoordinates(x, y);
                        final double value = grid.get(x, y);
                        return value == 0
                            ? ImmutableList.of()
                            : ImmutableList.of(
                                heatmapGatherer.getName(),
                                step,
                                coordinates.x,
                                coordinates.y,
                                value,
                                heatmapGatherer.getUnit()
                            );
                    })
                );
            })
        );
    }

    void registerHeatmapGatherer(
        Function<FishState, ? extends HeatmapGatherer> heatmapProducer
    ) {
        getModel().registerStartable(fishState -> {
            HeatmapGatherer heatmapGatherer = heatmapProducer.apply(fishState);
            heatmapGatherers.add(heatmapGatherer);
            heatmapGatherer.start(fishState);
        });
    }

    public FishState getModel() { return model; }

    void registerHeatmapGatherers(
        Function<FishState, Iterable<? extends HeatmapGatherer>> heatmapsProducer
    ) {
        getModel().registerStartable(fishState ->
            heatmapsProducer.apply(fishState).forEach(heatmapGatherer -> {
                heatmapGatherers.add(heatmapGatherer);
                heatmapGatherer.start(fishState);
            })
        );
    }

}
