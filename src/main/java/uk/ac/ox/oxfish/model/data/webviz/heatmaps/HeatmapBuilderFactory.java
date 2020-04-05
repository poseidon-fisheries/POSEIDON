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

import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.webviz.JsonBuilder;
import uk.ac.ox.oxfish.model.data.webviz.JsonBuilderFactory;
import uk.ac.ox.oxfish.model.data.webviz.scenarios.ColourMapEntry;
import uk.ac.ox.oxfish.model.data.webviz.scenarios.HeatmapDefinition;

import java.util.Collection;
import java.util.function.ToDoubleFunction;

public interface HeatmapBuilderFactory extends
    JsonBuilderFactory<Heatmap>,
    JsonBuilder<HeatmapDefinition> {

    String getTitle();

    JsonBuilderFactory<Collection<ColourMapEntry>> getColourMapBuilderFactory();

    ToDoubleFunction<SeaTile> makeNumericExtractor(FishState fishState);

    @Override default String getBaseName() { return getTitle() + " Heatmap"; }

    TimestepsBuilder makeTimestepsBuilder();

    @Override default HeatmapBuilder apply(final FishState fishState) {
        return new HeatmapBuilder(makeNumericExtractor(fishState), makeTimestepsBuilder());
    }

    @Override default HeatmapDefinition buildJsonObject(final FishState fishState) {
        return new HeatmapDefinition(
            getTitle(),
            getFileName(),
            getColourMapBuilderFactory().apply(fishState).buildJsonObject(fishState)
        );
    }

}
