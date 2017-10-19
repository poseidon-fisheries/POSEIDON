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
import uk.ac.ox.oxfish.geography.discretization.MapDiscretization;
import uk.ac.ox.oxfish.model.FishState;

/**
 * Returns 1 if the tile is in the same group and 0 otherwise
 * Created by carrknight on 8/7/17.
 */
public class GroupDummyExtractor implements ObservationExtractor{


    /**
     * returns 1 if the tile is in this group and 0 otherwise
     */
    final private int group;

    final private MapDiscretization discretization;


    public GroupDummyExtractor(int group, MapDiscretization discretization) {
        this.group = group;
        this.discretization = discretization;
    }

    @Override
    public double extract(SeaTile tile, double timeOfObservation, Fisher agent, FishState model) {
        if(discretization.getGroup(tile)==group)
            return 1;
        else
            return 0;
    }
}
