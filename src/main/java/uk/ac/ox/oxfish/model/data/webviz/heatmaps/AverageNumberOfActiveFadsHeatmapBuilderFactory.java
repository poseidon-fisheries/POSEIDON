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

import uk.ac.ox.oxfish.geography.fads.FadMap;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.webviz.JsonBuilder;
import uk.ac.ox.oxfish.model.data.webviz.JsonDefinitionBuilderFactory;
import uk.ac.ox.oxfish.model.data.webviz.scenarios.ColourMapEntry;

import java.util.Collection;

public class AverageNumberOfActiveFadsHeatmapBuilderFactory implements HeatmapBuilderFactory {

    private int interval = 30;
    private String colour = "yellow";
    private AveragingTimestepsBuilder timestepsBuilder = null;

    @Override public String getTitle() { return "Average daily number of active FADs"; }

    @Override public JsonDefinitionBuilderFactory<Collection<ColourMapEntry>> getColourMapBuilderFactory() {
        return new MonochromeGradientColourMapBuilderFactory(colour, () -> timestepsBuilder.getMaxValueSeen());
    }

    @SuppressWarnings("unused") public int getInterval() { return interval; }

    @SuppressWarnings("unused") public void setInterval(int interval) { this.interval = interval; }

    @Override public JsonBuilder<Heatmap> makeDataBuilder(FishState fishState) {
        final FadMap fadMap = fishState.getFadMap();
        timestepsBuilder = new AveragingTimestepsBuilder(interval);
        return new ExtractorBasedHeatmapBuilder(
            seaTile -> fadMap.fadsAt(seaTile).numObjs,
            timestepsBuilder
        );
    }

    public String getColour() { return colour; }

    public void setColour(final String colour) { this.colour = colour; }

}
