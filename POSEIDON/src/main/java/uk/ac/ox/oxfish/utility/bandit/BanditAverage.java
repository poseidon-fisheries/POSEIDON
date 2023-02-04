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

package uk.ac.ox.oxfish.utility.bandit;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.Averager;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.function.Supplier;

/**
 * Helper method to keep track of the average reward (and number of observations)
 * Created by carrknight on 11/10/16.
 */
public class BanditAverage {


    private final Averager<Double>[] averages;
    private final int[] observations;


    /**
     * supplier constructor
     * @param numberOfArms
     * @param constructor
     */
    public BanditAverage(int numberOfArms, Supplier<Averager<Double>> constructor)
    {
        averages = new Averager[numberOfArms];
        observations = new int[numberOfArms];
        for(int i=0; i<numberOfArms; i++)
            averages[i] = constructor.get();
    }


    /**
     * factory constructor
     */
    public BanditAverage(int numberOfArms,
                         AlgorithmFactory<? extends Averager> factory,
                         FishState state)
    {
        this(numberOfArms, () -> factory.apply(state));

    }


    public void observeReward(double reward, int arm)
    {
        averages[arm].addObservation(reward);
        observations[arm]++;
    }


    public int getNumberOfObservations(int arm)
    {
        return observations[arm];
    }

    public double getAverage(int arm)
    {
        return averages[arm].getSmoothedObservation();
    }

    public int getNumberOfArms(){
        return averages.length;
    }

}
