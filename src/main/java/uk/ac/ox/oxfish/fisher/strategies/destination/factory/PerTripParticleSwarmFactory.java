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

package uk.ac.ox.oxfish.fisher.strategies.destination.factory;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.selfanalysis.HourlyProfitInTripObjective;
import uk.ac.ox.oxfish.fisher.strategies.destination.FavoriteDestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.PerTripIterativeDestinationStrategy;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.adaptation.maximization.ParticleSwarmAlgorithm;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.UniformDoubleParameter;

import java.util.function.Predicate;

/**
 * Creates a trip strategy that uses PSO for imitating and randomly shocks velocity for exploration
 * Created by carrknight on 7/28/15.
 */
public class PerTripParticleSwarmFactory implements AlgorithmFactory<PerTripIterativeDestinationStrategy>
{

    private DoubleParameter explorationShockSize = new FixedDoubleParameter(4d);

    private DoubleParameter explorationProbability = new FixedDoubleParameter(.3d);


    private DoubleParameter  memoryWeight = new UniformDoubleParameter(.5,1);

    private DoubleParameter  friendWeight = new UniformDoubleParameter(.5,1);

    private DoubleParameter inertia = new UniformDoubleParameter(.3,.8);


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public PerTripIterativeDestinationStrategy apply(FishState state) {
        MersenneTwisterFast random = state.random;
        NauticalMap map = state.getMap();


        return new PerTripIterativeDestinationStrategy(
                new FavoriteDestinationStrategy(map,random),
                ParticleSwarmAlgorithm.defaultSeatileParticleSwarm(inertia.apply(random).floatValue(),
                                                                   memoryWeight.apply(random).floatValue(),
                                                                   friendWeight.apply(random).floatValue(),
                                                                   explorationShockSize.apply(random).floatValue(),
                                                                   new DoubleParameter[]{
                                                                           new UniformDoubleParameter(
                                                                                   -map.getWidth() / 5,
                                                                                   map.getWidth() / 5),
                                                                           new UniformDoubleParameter(
                                                                                   -map.getHeight() / 5,
                                                                                   map.getHeight() / 5)},
                                                                   random,map.getWidth(),map.getHeight()),
                explorationProbability.apply(random), 1, new HourlyProfitInTripObjective(), new Predicate<SeaTile>() {
            @Override
            public boolean test(SeaTile a) {
                return true;
            }
        });

    }


    public DoubleParameter getExplorationShockSize() {
        return explorationShockSize;
    }

    public void setExplorationShockSize(DoubleParameter explorationShockSize) {
        this.explorationShockSize = explorationShockSize;
    }

    public DoubleParameter getExplorationProbability() {
        return explorationProbability;
    }

    public void setExplorationProbability(DoubleParameter explorationProbability) {
        this.explorationProbability = explorationProbability;
    }

    public DoubleParameter getMemoryWeight() {
        return memoryWeight;
    }

    public void setMemoryWeight(DoubleParameter memoryWeight) {
        this.memoryWeight = memoryWeight;
    }

    public DoubleParameter getFriendWeight() {
        return friendWeight;
    }

    public void setFriendWeight(DoubleParameter friendWeight) {
        this.friendWeight = friendWeight;
    }

    public DoubleParameter getInertia() {
        return inertia;
    }

    public void setInertia(DoubleParameter inertia) {
        this.inertia = inertia;
    }
}
