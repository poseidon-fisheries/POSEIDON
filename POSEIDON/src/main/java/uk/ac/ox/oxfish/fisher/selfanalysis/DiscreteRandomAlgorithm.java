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
import uk.ac.ox.oxfish.utility.adaptation.Sensor;
import uk.ac.ox.oxfish.utility.adaptation.maximization.AdaptationAlgorithm;

import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Function;

import static uk.ac.ox.oxfish.utility.FishStateUtilities.entry;

/**
 * A simple exploration-imitaiton-exploitation decision where the random part occurs by choosing from a list
 * Created by carrknight on 8/6/15.
 */
public class DiscreteRandomAlgorithm<T> implements AdaptationAlgorithm<T> {


    private final Function<Entry<T, MersenneTwisterFast>, T> randomChooser;


    public DiscreteRandomAlgorithm(
        final List<T> randomChoices
    ) {
        this.randomChooser = pair -> {
            if (randomChoices.isEmpty()) //if there is nothing randomizable, don't bother
                return pair.getKey();
            //otherwise randomize!
            return randomChoices.get(pair.getValue().nextInt(randomChoices.size()));
        };
    }

    @Override
    public T randomize(
        final MersenneTwisterFast random, final Fisher agent, final double currentFitness, final T current
    ) {
        return randomChooser.apply(entry(current, random));
    }


    @Override
    public Entry<T, Fisher> imitate(
        final MersenneTwisterFast random,
        final Fisher agent,
        final double fitness,
        final T current,
        final Collection<Fisher> friends,
        final ObjectiveFunction<Fisher> objectiveFunction,
        final Sensor<Fisher, T> sensor
    ) {
        return FishStateUtilities.imitateFriendAtRandom(random, fitness,
            current, friends,
            objectiveFunction, sensor, agent
        );


    }


    @Override
    public T exploit(
        final MersenneTwisterFast random,
        final Fisher agent,
        final double currentFitness,
        final T current
    ) {
        return current; //nothing happens
    }


    /**
     * returns null
     */
    @Override
    public T judgeRandomization(
        final MersenneTwisterFast random,
        final Fisher agent,
        final double previousFitness,
        final double currentFitness,
        final T previous,
        final T current
    ) {
        return null;
    }

    /**
     * returns null
     */
    @Override
    public T judgeImitation(
        final MersenneTwisterFast random,
        final Fisher agent,
        final Fisher friendImitated,
        final double fitnessBeforeImitating,
        final double fitnessAfterImitating,
        final T previous,
        final T current
    ) {
        return null;
    }

    @Override
    public void start(final FishState model, final Fisher agent, final T initial) {
        //nothing, no need for a setup
    }
}
