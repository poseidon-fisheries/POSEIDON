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
import uk.ac.ox.oxfish.geography.Distance;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

/**
 * Differences in distance from port is what defines this distance
 * Created by carrknight on 7/7/16.
 */
public class PortDistanceExtractor implements ObservationExtractor {


    /**
     * distance metric to use, if null, use map distance instead
     */
    private final Distance geographicalDistance;

    /**
     * offset get summed up to all geographical distances (usually to avoid 0s if you think you are taking logs or things like that)
     */
    private final double offset;


    public PortDistanceExtractor(Distance geographicalDistance, final double offset) {

        this.geographicalDistance = geographicalDistance;
        this.offset = offset;
    }

    public PortDistanceExtractor() {
        this.geographicalDistance = null;
        this.offset = 0;
    }

    @Override
    public double extract(SeaTile tile, double timeOfObservation, Fisher agent, FishState model) {
        SeaTile portLocation = agent.getHomePort().getLocation();

        if (geographicalDistance != null)
            return geographicalDistance.distance(portLocation, tile, model.getMap()) + offset;
        else
            return model.getMap().distance(portLocation, tile) + offset;


    }


}
