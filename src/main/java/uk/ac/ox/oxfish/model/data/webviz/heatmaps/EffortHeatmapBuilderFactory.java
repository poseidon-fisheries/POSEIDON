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
import uk.ac.ox.oxfish.model.data.webviz.JsonBuilder;

public class EffortHeatmapBuilderFactory extends AbstractIntervalHeatmapBuilderFactory {

    private SummingTimestepsBuilder timestepsBuilder = null; // will be set when the builder is constructed

    public EffortHeatmapBuilderFactory(final int interval, final String colour) {
        super(interval, colour);
    }

    @Override public String getTitle() {
        return "Number of trawls per " + getInterval() + " day intervals";
    }

    @Override public MonochromeGradientColourMapBuilderFactory getColourMapBuilderFactory() {
        return new MonochromeGradientColourMapBuilderFactory(getColour(), () -> timestepsBuilder.getMaxValueSeen());
    }

    @Override public JsonBuilder<Heatmap> makeDataBuilder(FishState fishState) {
        this.timestepsBuilder = new SummingTimestepsBuilder(getInterval());
        return new ExtractorBasedHeatmapBuilder(
            seaTile -> fishState.getDailyTrawlsMap().get(seaTile.getGridX(), seaTile.getGridY()),
            this.timestepsBuilder
        );
    }

}
