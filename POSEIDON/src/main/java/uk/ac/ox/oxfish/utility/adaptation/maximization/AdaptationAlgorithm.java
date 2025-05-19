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

package uk.ac.ox.oxfish.utility.adaptation.maximization;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.selfanalysis.ObjectiveFunction;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.adaptation.Sensor;

import java.util.Collection;
import java.util.Map.Entry;

/**
 * The algorithmic part of the adaptation routine of agents. There are always 3 possible actions:
 * <ul>
 *     <li> Randomization</li>
 *     <li> Imitation</li>
 *     <li> Exploitation</li>
 * </ul>
 * <p>
 *  Probabilities are sequential but only one action is ever taken. So if the probabilities of exploration and imitation
 *  are both at 70% then there is a 70% of exploring and a (.3)(.7) probability of imitating. Failing both the agent will
 *  exploit
 * <p>
 * Created by carrknight on 8/6/15.
 */
public interface AdaptationAlgorithm<T> {


    void start(FishState model, Fisher agent, T initial);


    T randomize(
        MersenneTwisterFast random, Fisher agent, double currentFitness,
        T current
    );

    /**
     * if you have explored in the previous step, this gets called to make you judge exploration (you might want to
     * backtrack). Return null if you don't want to backtrack and skip directly to another round of exploration-exploitation
     */

    T judgeRandomization(
        MersenneTwisterFast random, Fisher agent,
        double previousFitness, double currentFitness,
        T previous, T current
    );

    /**
     * if you have imitated in the previous step, this gets called for you to judge imitation (you might want to backtrack)
     * Return null if you don't want to backtrack and skip directly to another round of exploration-exploitation
     */
    T judgeImitation(
        MersenneTwisterFast random, Fisher agent,
        Fisher friendImitated,
        double fitnessBeforeImitating,
        double fitnessAfterImitating,
        T previous, T current
    );

    /**
     * asks the agent to imitate someone.
     *
     * @param random            the randomizer
     * @param agent             the agent who has to imitate
     * @param fitness           his current fitness
     * @param current           his current decision
     * @param friends           the collection of friends he has
     * @param objectiveFunction the objective function by which the agent judges himself and others
     * @param sensor            the function that maps Fisher--->current decision
     * @return a pair of the new decision to take AND the friend imitated (which can be null)
     */
    Entry<T, Fisher> imitate(
        MersenneTwisterFast random,
        Fisher agent, double fitness,
        T current,
        Collection<Fisher> friends,
        ObjectiveFunction<Fisher> objectiveFunction,
        Sensor<Fisher, T> sensor
    );


    T exploit(
        MersenneTwisterFast random,
        Fisher agent, double currentFitness,
        T current
    );


}
