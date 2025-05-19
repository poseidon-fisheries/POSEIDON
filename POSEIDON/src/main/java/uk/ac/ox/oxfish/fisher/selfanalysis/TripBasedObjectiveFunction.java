/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2025, University of Oxford.
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

package uk.ac.ox.oxfish.fisher.selfanalysis;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.log.TripRecord;

/**
 * A simple abstract class that judges the objective given the last or secondlast trip. How to turn the tripRecord
 * into a utility function depends on the subclasses
 * Created by carrknight on 3/24/16.
 */
public abstract class TripBasedObjectiveFunction implements ObjectiveFunction<Fisher> {


    /**
     * compute current fitness of the agent
     *
     * @param observer
     * @param observed agent whose fitness we are trying to compute
     * @return a fitness value: the higher the better
     */
    @Override
    public double computeCurrentFitness(Fisher observer, Fisher observed) {
        TripRecord lastFinishedTrip = observed.getLastFinishedTrip();
        if (lastFinishedTrip == null) //don't bother if there is nothing to look at
            return Double.NaN;

        //if you are guessing the fitness of a trip that departed elsewhere, you need to simulate
        //how much would it take you to get there. Otherwise just use the trip record straight:

        //don't bother copying if discarding strategy is different!
        if (observed.getDiscardingStrategy() != null && observer.getDiscardingStrategy() != null &&
            !(observed.getDiscardingStrategy().getClass().equals(observer.getDiscardingStrategy().getClass())))
            return Double.NaN;

            //if you are looking at yourself, just look at recorded profits
        else
            return extractUtilityFromTrip(observer, lastFinishedTrip, observed);


    }


    abstract protected double extractUtilityFromTrip(
        Fisher observer,
        TripRecord tripRecord,
        Fisher Observed
    );
}
