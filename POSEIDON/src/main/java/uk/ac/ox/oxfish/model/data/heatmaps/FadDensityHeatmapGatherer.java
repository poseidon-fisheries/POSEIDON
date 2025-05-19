/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2020-2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.model.data.heatmaps;

import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.fads.FadMap;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.data.heatmaps.mergers.IterativeAverageMerger;

import java.util.function.ToDoubleFunction;

public class FadDensityHeatmapGatherer extends HeatmapGatherer {

    private static final long serialVersionUID = -5707936919764634160L;

    public FadDensityHeatmapGatherer(
        final int interval
    ) {
        super(
            "FAD density",
            "Average number of FADs",
            interval,
            new Extractor(),
            heatmapGatherer -> new IterativeAverageMerger(heatmapGatherer::getNumObservations)
        );
    }

    private static class Extractor implements ToDoubleFunction<SeaTile>, Startable {

        private FadMap fadMap = null;

        @Override
        public void start(final FishState fishState) {
            fadMap = fishState.getFadMap();
        }

        @Override
        public double applyAsDouble(final SeaTile seaTile) {
            return fadMap.fadsAt(seaTile).numObjs;
        }

    }

}
