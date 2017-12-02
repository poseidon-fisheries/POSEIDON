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

package uk.ac.ox.oxfish.fisher.selfanalysis;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;

/**
 * A simple abstract class that judges the objective given the last or secondlast trip. How to turn the tripRecord
 * into a utility function depends on the subclasses
 * Created by carrknight on 3/24/16.
 */
public abstract class TripBasedObjectiveFunction implements ObjectiveFunction<Fisher> {


    /**
     * compute current fitness of the agent
     *
     *
     * @param observer
     * @param observed agent whose fitness we are trying to compute
     * @return a fitness value: the higher the better
     */
    @Override
    public double computeCurrentFitness(Fisher observer, Fisher observed) {
        TripRecord lastFinishedTrip = observed.getLastFinishedTrip();
        if(lastFinishedTrip == null) //don't bother if there is nothing to look at
            return Double.NaN;

        //if you are guessing the fitness of a trip that departed elsewhere, you need to simulate
        //how much would it take you to get there. Otherwise just use the trip record straight:

        //if you are looking at yourself, just look at recorded profits
        if(observer == observed)
            return extractUtilityFromTrip(observer,lastFinishedTrip,observed);
        else
        {

            //don't bother copying if discarding strategy is different!
            if(!(observed.getDiscardingStrategy().getClass().equals(observer.getDiscardingStrategy().getClass())) )
                return Double.NaN;


            //if they are from the same port, then again return the memory
            if((
                    observed.getHomePort().equals(observer.getHomePort())) &&
                    (observed.getGear().isSame(observer.getGear())))
                return extractUtilityFromTrip(observer,lastFinishedTrip,observed);
            else
            //otherwise simulate!
            {
                //compute empirical CPUE
                double[] cpue = lastFinishedTrip.getTotalCatch();
                for(int i=0; i<cpue.length; i++)
                    cpue[i] = cpue[i]/ (double)lastFinishedTrip.getEffort();
                if(lastFinishedTrip.getMostFishedTileInTrip()==null)
                    return Double.NaN;
                //use it to simulate a trip
                TripRecord simulatedTrip = LameTripSimulator.simulateRecord(
                        observer,
                        lastFinishedTrip.getMostFishedTileInTrip(),
                        observer.grabState(),
                        5 * 24d,//todo change this
                        cpue
                );
                if(simulatedTrip == null) //if area is unreacheable from our port
                    return Double.NaN;
                //extract utility from SIMULATED trip
                return extractUtilityFromTrip(observer,simulatedTrip,observed);
            }


        }

    }



    abstract protected double extractUtilityFromTrip(
            Fisher observer,
            TripRecord tripRecord,
            Fisher Observed);
}
