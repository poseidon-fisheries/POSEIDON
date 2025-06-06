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
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.adaptation.Sensor;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.function.Predicate;

import static uk.ac.ox.oxfish.utility.FishStateUtilities.entry;

/**
 * An hill=climber that tries a new step on "randomize", copy a friend in "imitate" and stays put in "exploit".
 * <p>
 * It is abstract as it expects a method to randomize a new step
 * Created by carrknight on 8/6/15.
 */
public class BeamHillClimbing<T> implements AdaptationAlgorithm<T> {


    /**
     * the default value of copyAlwaysBest
     */
    public final static boolean DEFAULT_ALWAYS_COPY_BEST = true;

    public final static boolean DEFAULT_BACKTRACKS_ON_BAD_EXPLORATION = true;
    /**
     * the default state of the unfriendPredicate field
     */
    public final static Predicate<Entry<Double, Double>> DEFAULT_DYNAMIC_NETWORK =
        doubleDoublePair -> false;
    /**
     * if true imitation occurs by looking at your friend who is performing better, otherwise it
     * works by looking at a random friend. <br>
     * In both cases we ignore friends who perform worse or equal to us
     */
    private final boolean copyAlwaysBest;
    /**
     * A function that judges whether to change a friend after imitating given the pair (previous fitness,newfitness).
     * When the function returns true, we will replace whoever we imitated with somebody at random
     */
    private final Predicate<Entry<Double, Double>> unfriendPredicate;
    /**
     * what is the result of an exploration step
     */
    private final RandomStep<T> randomStep;
    /**
     * if an exploration goes badly, does it go back to the previous spot ("the best" remembered)
     */
    private final boolean backtracksOnBadExploration;
    private FishState model;

    public BeamHillClimbing(final RandomStep<T> randomStep) {
        this(DEFAULT_ALWAYS_COPY_BEST,
            true, DEFAULT_DYNAMIC_NETWORK,
            randomStep
        );
    }

    public BeamHillClimbing(
        final boolean copyAlwaysBest,
        final boolean backtracksOnBadExploration,
        final Predicate<Entry<Double, Double>> unfriendPredicate,
        final RandomStep<T> randomStep
    ) {
        this.copyAlwaysBest = copyAlwaysBest;
        this.unfriendPredicate = unfriendPredicate;
        this.randomStep = randomStep;
        this.backtracksOnBadExploration = backtracksOnBadExploration;
    }

    @Override
    public void start(final FishState model, final Fisher agent, final T initial) {
        this.model = model;
    }

    /**
     * new random step
     */
    @Override
    public T randomize(
        final MersenneTwisterFast random,
        final Fisher agent,
        final double currentFitness,
        final T current
    ) {
        return randomStep(this.model, random, agent, current);
    }

    public T randomStep(final FishState state, final MersenneTwisterFast random, final Fisher fisher, final T current) {
        return randomStep.randomStep(state, random, fisher, current);

    }

    /**
     * copy friend. No problem
     */
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
        if (copyAlwaysBest)
            return FishStateUtilities.imitateBestFriend(random, agent,
                fitness, current,
                friends, objectiveFunction, sensor
            );
        else
            return FishStateUtilities.imitateFriendAtRandom(random, fitness,
                current, friends,
                objectiveFunction, sensor, agent
            );
    }

    @Override
    public T judgeRandomization(
        final MersenneTwisterFast random,
        final Fisher agent,
        final double previousFitness,
        final double currentFitness,
        final T previous,
        final T current
    ) {
        if (backtracksOnBadExploration && previousFitness > currentFitness)
            return previous;
        else
            return current;
    }

    //stay still!
    @Override
    public T exploit(
        final MersenneTwisterFast random,
        final Fisher agent,
        final double currentFitness,
        final T current
    ) {
        return current;
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
        if (unfriendPredicate.test(entry(fitnessBeforeImitating, fitnessAfterImitating)))
            agent.replaceFriend(friendImitated, true);
        if (fitnessBeforeImitating > fitnessAfterImitating)
            return previous;
        else
            return current;
    }
}
