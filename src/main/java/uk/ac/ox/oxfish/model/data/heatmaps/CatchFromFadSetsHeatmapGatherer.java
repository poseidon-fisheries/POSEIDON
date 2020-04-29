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

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.actions.purseseiner.MakeFadSet;
import uk.ac.ox.oxfish.model.data.heatmaps.extractors.CatchFromSetExtractor;
import uk.ac.ox.oxfish.model.data.heatmaps.mergers.SummingMerger;
import uk.ac.ox.oxfish.model.data.monitors.observers.FadSetActionObserver;

public class CatchFromFadSetsHeatmapGatherer extends HeatmapGatherer {

    public CatchFromFadSetsHeatmapGatherer(
        final int interval,
        final Species species
    ) {
        super(
            species.getName() + " catch from FAD sets",
            "Catch (t)",
            interval,
            new Extractor(species),
            SummingMerger.INSTANCE
        );
    }

    private static class Extractor
        extends CatchFromSetExtractor<MakeFadSet>
        implements FadSetActionObserver {

        Extractor(final Species species) { super(species); }

    }

}
