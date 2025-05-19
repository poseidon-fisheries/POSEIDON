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

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.adaptation.Adaptation;
import uk.ac.ox.oxfish.utility.adaptation.ExploreImitateAdaptation;
import uk.ac.ox.oxfish.utility.adaptation.Sensor;
import uk.ac.ox.oxfish.utility.adaptation.maximization.BeamHillClimbing;
import uk.ac.ox.oxfish.utility.adaptation.probability.FixedProbability;

/**
 * Created by carrknight on 2/14/17.
 */
public class SocialNetworkAdaptation implements Adaptation {


    private final ExploreImitateAdaptation<Integer> delegate;


    public SocialNetworkAdaptation(
        final int stepSize,
        final ObjectiveFunction<Fisher> objective,
        final double explorationProbability
    ) {


        delegate =
            new ExploreImitateAdaptation<Integer>(
                //don't change friends if you've been stuck a lot of time at home

                fisher1 -> fisher1.getHoursAtPort() < 10 * 24,
                new BeamHillClimbing<Integer>((state, random, fisher, current) -> Math.min(
                    Math.max(
                        random.nextBoolean() ?
                            current + random.nextInt(stepSize) + 1 :
                            current - random.nextInt(stepSize) - 1,
                        0
                    ),
                    state.getFishers().size() - 1
                ))
                ,
                (subject, policy, model) -> {
                    final int originalDirectedNeighbors = model.getSocialNetwork().getDirectedNeighbors(subject).size();
                    final int target = Math.min(Math.max(policy, 0), model.getFishers().size() - 1);
                    final int difference = target - model.getSocialNetwork().getBackingnetwork().getPredecessorCount(subject);
                    if (difference > 0) {
                        for (int i = 0; i < difference; i++)
                            model.getSocialNetwork()
                                .addRandomConnection(subject, model.getFishers(), new MersenneTwisterFast());
                    } else if (difference < 0) {
                        for (int i = 0; i < -difference; i++) {
                            model.getSocialNetwork().removeRandomConnection(subject, model.getRandom());
                        }
                    }
                    Preconditions.checkArgument(
                        model.getSocialNetwork().getBackingnetwork().getPredecessorCount(subject) == target);
                    assert originalDirectedNeighbors == model.getSocialNetwork().getDirectedNeighbors(subject).size();
                },
                (Sensor<Fisher, Integer>) system -> system.getSocialNetwork()
                    .getBackingnetwork()
                    .getPredecessorCount(system),
                objective,
                new FixedProbability(explorationProbability, 0),
                integer -> true


            );
    }


    @Override
    public void start(final FishState model, final Fisher fisher) {
        delegate.start(model, fisher);
    }

    @Override
    public void turnOff(final Fisher fisher) {
        delegate.turnOff(fisher);
    }

    /**
     * Ask yourself to adapt
     *
     * @param toAdapt who is doing the adaptation
     * @param state
     * @param random  the randomizer
     */
    @Override
    public void adapt(final Fisher toAdapt, final FishState state, final MersenneTwisterFast random) {
        delegate.adapt(toAdapt, state, random);
    }
}
