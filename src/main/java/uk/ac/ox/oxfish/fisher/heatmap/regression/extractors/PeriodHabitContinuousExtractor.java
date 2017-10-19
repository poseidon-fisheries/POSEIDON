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

import java.util.Iterator;
import java.util.LinkedList;

/**
 * Created by carrknight on 7/17/17.
 */
public class PeriodHabitContinuousExtractor implements ObservationExtractor {
    private final MapDiscretization discretization;
    private final int period;



    public PeriodHabitContinuousExtractor(MapDiscretization discretization,
                                       final int period) {
        this.discretization = discretization;
        this.period = period;
    }

    @Override
    public double extract(
            SeaTile tile, double timeOfObservation, Fisher agent, FishState model) {
        //it it has been less than ```period``` days since you went there, you get the habit bonus!
        return  count(model.getDay(),
                      agent.getDiscretizedLocationMemory().getVisits(
                              (discretization.getGroup(tile))
                      ));
    }


    /**
     * goes through all the visits and checks how many happened between today and today-period (bound not included)
     * @param currentDay simulation day
     * @param visits queue containing all visits (last ones are more recent)
     * @return number of visits that occurred between (currentDay-period,currentDay]
     */
    public int count(int currentDay,
                     LinkedList<Integer> visits)
    {
        int sum = 0;
        Iterator<Integer> iterator = visits.descendingIterator();
        while(iterator.hasNext())
        {
            Integer visit = iterator.next();
            if(visit> currentDay-period)
                sum++;
            else
                return sum;
        }
        return sum;


    }

}
