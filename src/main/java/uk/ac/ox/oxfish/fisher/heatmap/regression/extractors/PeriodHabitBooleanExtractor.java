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
 * If I have been to this area (discretized) over the past ```period``` days, that's 1 otherwise 0
 * Created by carrknight on 2/13/17.
 */
public class PeriodHabitBooleanExtractor implements ObservationExtractor {


    private final MapDiscretization discretization;
    private final int period;



    public PeriodHabitBooleanExtractor(MapDiscretization discretization,
                                       final int period) {
        this.discretization = discretization;
        this.period = period;
    }

    @Override
    public double extract(
            SeaTile tile, double timeOfObservation, Fisher agent, FishState model) {
        //it it has been less than ```period``` days since you went there, you get the habit bonus!
        return  model.getDay() -
                agent.getDiscretizedLocationMemory()
                        .getLastDayVisited(discretization.getGroup(tile)) < period ?
                1 : 0;
    }
}
