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

import uk.ac.ox.oxfish.fisher.actions.purseseiner.PurseSeinerAction;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.heatmaps.extractors.ActionCountingExtractor;
import uk.ac.ox.oxfish.model.data.webviz.JsonBuilder;

abstract class ActionCountingHeatmapBuilderFactory<A extends PurseSeinerAction>
    extends AbstractIntervalHeatmapBuilderFactory {

    ActionCountingHeatmapBuilderFactory() { super(30, "pink"); }

    @Override public MonochromeGradientColourMapBuilderFactory getColourMapBuilderFactory() {
        return new MonochromeGradientColourMapBuilderFactory(
            getColour(),
            () -> getTimestepsBuilder().getMaxValueSeen()
        );
    }

    @Override public JsonBuilder<Heatmap> makeDataBuilder(FishState ignored) {
        setTimestepsBuilder(new SummingTimestepsBuilder(getInterval()));
        return new ExtractorBasedHeatmapBuilder(makeExtractor(), getTimestepsBuilder());
    }

    abstract ActionCountingExtractor<A> makeExtractor();

}
