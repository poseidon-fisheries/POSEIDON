/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.fisher.heatmap.regression.extractors;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

/**
 * class that gets an observation (Seatile,Time,Fisher,Fishstate) and returns a number representing the feature to extract
 * Created by carrknight on 8/18/16.
 */
public interface ObservationExtractor {

    /**
     * takes a series of extractors and returns it as an array of numerical features
     */
    static double[] convertToFeatures(
        SeaTile tile, double timeOfObservation,
        Fisher fisher, ObservationExtractor[] extractors, FishState model
    ) {
        double[] observation = new double[extractors.length];
        for (int i = 0; i < observation.length; i++)
            observation[i] = extractors[i].extract(tile, timeOfObservation, fisher, model);
        return observation;
    }

    public double extract(SeaTile tile, double timeOfObservation, Fisher agent, FishState model);

}
