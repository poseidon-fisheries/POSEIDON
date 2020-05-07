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

package uk.ac.ox.oxfish.model.data.heatmaps;

import com.google.common.collect.ImmutableList;
import com.vividsolutions.jts.geom.Coordinate;
import org.jetbrains.annotations.NotNull;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.field.grid.DoubleGrid2D;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.data.monitors.loggers.RowProvider;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.DoubleBinaryOperator;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.lang.Double.NaN;
import static java.util.stream.IntStream.range;

public class HeatmapGatherer implements AdditionalStartable, Steppable, RowProvider {

    private static final List<String> HEADERS = ImmutableList.of("name", "step", "lon", "lat", "value", "unit");
    private final String name;
    private final String unit;
    private final int interval;
    private final ToDoubleFunction<SeaTile> numericExtractor;
    private final DoubleBinaryOperator merger;
    private final Map<Integer, DoubleGrid2D> grids = new HashMap<>();
    private int numObservations = 0;
    private int intervalStartDay = 0;
    private DoubleGrid2D currentGrid;
    private FishState fishState;

    HeatmapGatherer(
        final String name,
        final String unit,
        final int interval,
        final ToDoubleFunction<SeaTile> numericExtractor,
        final Function<HeatmapGatherer, DoubleBinaryOperator> mergerConstructor
    ) {
        this.name = name;
        this.unit = unit;
        this.interval = interval;
        this.numericExtractor = numericExtractor;
        this.merger = mergerConstructor.apply(this);
    }

    HeatmapGatherer(
        final String name,
        final String unit,
        final int interval,
        final ToDoubleFunction<SeaTile> numericExtractor,
        final DoubleBinaryOperator merger
    ) {
        this.name = name;
        this.unit = unit;
        this.interval = interval;
        this.numericExtractor = numericExtractor;
        this.merger = merger;
    }

    int getNumObservations() { return numObservations; }

    public double maxValueSeen() {
        return grids.values().stream().mapToDouble(DoubleGrid2D::max).max().orElse(NaN);
    }

    @Override public void step(final SimState simState) {

        final NauticalMap map = fishState.getMap();

        numObservations++;
        if (numObservations == 1) {
            // The first observation resets the grid and
            // make note of the starting day for the new interval
            currentGrid = extractValues(map);
            intervalStartDay = fishState.getDay();
        } else {
            // Afterwards, we update the grid using the merge function
            mergeGrid(currentGrid, extractValues(map));
        }

        // When we have all the observations we need, we store the current grid
        // and reset the number of observations in order to start anew on the next call
        if (numObservations == interval) {
            grids.put(intervalStartDay, currentGrid);
            numObservations = 0;
        }
    }

    private DoubleGrid2D extractValues(NauticalMap map) {
        DoubleGrid2D grid = new DoubleGrid2D(map.getWidth(), map.getHeight());
        for (int x = 0; x < grid.getWidth(); ++x) {
            for (int y = 0; y < grid.getHeight(); ++y) {
                grid.set(x, y, numericExtractor.applyAsDouble(map.getSeaTile(x, y)));
            }
        }
        return grid;
    }

    /**
     * Mutates the old grid!
     */
    private void mergeGrid(DoubleGrid2D oldGrid, DoubleGrid2D newGrid) {
        for (int x = 0; x < oldGrid.getWidth(); ++x) {
            for (int y = 0; y < newGrid.getHeight(); ++y) {
                oldGrid.set(x, y, merger.applyAsDouble(oldGrid.get(x, y), newGrid.get(x, y)));
            }
        }
    }

    @Override public void start(final FishState fishState) {
        this.fishState = fishState;
        if (numericExtractor instanceof Startable)
            ((Startable) numericExtractor).start(fishState);
        fishState.scheduleEveryStep(this, StepOrder.DAILY_DATA_GATHERING);
    }

    @Override public List<String> getHeaders() { return HEADERS; }

    @Override public Iterable<List<?>> getRows() {
        return getGrids().entrySet().stream().flatMap(entry -> {
            final Integer step = entry.getKey();
            final DoubleGrid2D grid = entry.getValue();
            return range(0, grid.getWidth()).boxed().flatMap(x ->
                range(0, grid.getHeight()).boxed().flatMap(y ->
                    makeRow(step, grid, x, y)
                )
            );
        }).collect(toImmutableList());
    }

    public Map<Integer, DoubleGrid2D> getGrids() { return Collections.unmodifiableMap(grids); }

    @NotNull private Stream<List<?>> makeRow(final int step, final DoubleGrid2D grid, final int x, final int y) {
        final Coordinate coordinates = fishState.getMap().getCoordinates(x, y);
        final double value = grid.get(x, y);
        return value == 0
            ? Stream.empty()
            : Stream.of(ImmutableList.of(
                getName(),
                step,
                coordinates.x,
                coordinates.y,
                value,
                getUnit()
            ));
    }

    public String getName() { return name; }

    public String getUnit() { return unit; }

}
