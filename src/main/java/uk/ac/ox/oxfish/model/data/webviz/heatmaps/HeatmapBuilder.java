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

package uk.ac.ox.oxfish.model.data.webviz.heatmaps;

import sim.field.grid.DoubleGrid2D;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.heatmaps.HeatmapGatherer;
import uk.ac.ox.oxfish.model.data.webviz.SteppableJsonBuilder;

import java.util.Collection;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.stream.IntStream.range;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.round;

public final class HeatmapBuilder implements SteppableJsonBuilder<Heatmap> {

    private final HeatmapGatherer heatmapGatherer;

    HeatmapBuilder(final HeatmapGatherer heatmapGatherer) {
        this.heatmapGatherer = heatmapGatherer;
    }

    @Override
    public Heatmap buildJsonObject(final FishState fishState) {
        return new Heatmap(gridsToTimesteps());
    }

    private Collection<Timestep> gridsToTimesteps() {
        return heatmapGatherer.getGrids().entrySet().stream().map(entry ->
            new Timestep(entry.getKey(), gridToArray(entry.getValue()))
        ).collect(toImmutableList());
    }

    private static double[] gridToArray(final DoubleGrid2D grid) {
        return range(0, grid.getHeight()).boxed().flatMapToDouble(y ->
            range(0, grid.getWidth()).mapToDouble(x ->
                round(grid.get(x, y))
            )
        ).toArray();
    }

    @Override public void start(final FishState fishState) {
        heatmapGatherer.start(fishState);
    }

}
