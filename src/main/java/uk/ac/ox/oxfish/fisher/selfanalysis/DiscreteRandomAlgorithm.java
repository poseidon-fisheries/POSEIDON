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

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.Pair;
import uk.ac.ox.oxfish.utility.adaptation.Sensor;
import uk.ac.ox.oxfish.utility.adaptation.maximization.AdaptationAlgorithm;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * A simple exploration-imitaiton-exploitation decision where the random part occurs by choosing from a list
 * Created by carrknight on 8/6/15.
 */
public class DiscreteRandomAlgorithm<T> implements AdaptationAlgorithm<T>
{


    private final Function<Pair<T,MersenneTwisterFast>, T> randomChooser;


    public DiscreteRandomAlgorithm(
            List<T> randomChoices) {
        this.randomChooser = pair -> {
            if(randomChoices.isEmpty()) //if there is nothing randomizable, don't bother
                return pair.getFirst();
            //otherwise randomize!
            return randomChoices.get(pair.getSecond().nextInt(randomChoices.size()));
        };
    }

    @Override
    public T randomize(
            MersenneTwisterFast random, Fisher agent, double currentFitness, T current) {
        return randomChooser.apply(new Pair<T, MersenneTwisterFast>(current,random));
    }


    @Override
    public Pair<T,Fisher> imitate(
            MersenneTwisterFast random, Fisher agent, double fitness, T current, Collection<Fisher> friends,
            ObjectiveFunction<Fisher> objectiveFunction, Sensor<Fisher,T> sensor) {
        return FishStateUtilities.imitateFriendAtRandom(random, fitness,
                                                        current, friends,
                                                        objectiveFunction, sensor,agent );


    }


    @Override
    public T exploit(MersenneTwisterFast random, Fisher agent, double currentFitness, T current) {
        return current; //nothing happens
    }


    /**
     * returns null
     */
    @Override
    public T judgeRandomization(
            MersenneTwisterFast random, Fisher agent, double previousFitness, double currentFitness, T previous,
            T current) {
        return null;
    }

    /**
     * returns null
     */
    @Override
    public T judgeImitation(
            MersenneTwisterFast random, Fisher agent, Fisher friendImitated, double fitnessBeforeImitating,
            double fitnessAfterImitating, T previous,
            T current) {
        return null;
    }

    @Override
    public void start(FishState model, Fisher agent, T initial) {
        //nothing, no need for a setup
    }
}
