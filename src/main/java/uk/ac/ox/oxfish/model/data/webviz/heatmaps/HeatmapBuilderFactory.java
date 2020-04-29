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

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.heatmaps.HeatmapGatherer;
import uk.ac.ox.oxfish.model.data.webviz.JsonBuilder;
import uk.ac.ox.oxfish.model.data.webviz.JsonDataBuilderFactory;
import uk.ac.ox.oxfish.model.data.webviz.JsonDefinitionBuilderFactory;
import uk.ac.ox.oxfish.model.data.webviz.scenarios.HeatmapDefinition;

abstract public class HeatmapBuilderFactory
    implements JsonDataBuilderFactory<Heatmap>,
    JsonDefinitionBuilderFactory<HeatmapDefinition> {

    private int interval;
    private String colour;
    private HeatmapGatherer heatmapGatherer;

    HeatmapBuilderFactory() { this(30, "green"); }

    HeatmapBuilderFactory(final int interval, final String colour) {
        this.interval = interval;
        this.colour = colour;
    }

    @Override public String getBaseName() { return "Heatmap of " + getTitle(); }

    abstract public String getTitle();

    @Override public JsonBuilder<HeatmapDefinition> makeDefinitionBuilder(String scenarioTitle) {
        return fishState -> new HeatmapDefinition(
            getTitle(),
            makeFileName(scenarioTitle),
            getLegend(),
            getColourMapBuilderFactory().makeDefinitionBuilder(scenarioTitle).buildJsonObject(fishState)
        );
    }

    public String getLegend() { return getTitle(); }

    private MonochromeGradientColourMapBuilderFactory getColourMapBuilderFactory() {
        return new MonochromeGradientColourMapBuilderFactory(
            getColour(),
            heatmapGatherer::maxValueSeen
        );
    }

    public String getColour() { return colour; }

    public void setColour(final String colour) { this.colour = colour; }

    @Override public JsonBuilder<Heatmap> makeDataBuilder(FishState fishState) {
        heatmapGatherer = makeHeatmapGatherer(fishState);
        return new HeatmapBuilder(heatmapGatherer);
    }

    abstract HeatmapGatherer makeHeatmapGatherer(FishState fishState);

    public int getInterval() { return interval; }

    public void setInterval(final int interval) { this.interval = interval; }

}
