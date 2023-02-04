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

package uk.ac.ox.oxfish.fisher.strategies.departing;

import uk.ac.ox.oxfish.fisher.selfanalysis.GearImitationAnalysis;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.HashMap;

/**
 * Builds a fixed probability departing strategy but sets up a startable to have them adaptive
 * Created by carrknight on 3/22/16.
 */
public class AdaptiveProbabilityDepartingFactory implements AlgorithmFactory<FixedProbabilityDepartingStrategy> {

    private DoubleParameter initialProbabilityToLeavePort= new FixedDoubleParameter(0.5);

    private DoubleParameter explorationProbability = new FixedDoubleParameter(0.6);

    private DoubleParameter shockSize = new FixedDoubleParameter(0.6);

    private DoubleParameter imitationProbability = new FixedDoubleParameter(1);

    private boolean checkOnlyOnceADay = false;

    private final static HashMap<FishState,Startable> adapters = new HashMap<>();



    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public FixedProbabilityDepartingStrategy apply(FishState state) {
        FixedProbabilityDepartingStrategy toReturn = new FixedProbabilityDepartingStrategy(
                initialProbabilityToLeavePort.apply(state.random), checkOnlyOnceADay);

        //only once per model, please
        if(adapters.get(state) == null)
        {
            adapters.put(state, new Startable() {
                @Override
                public void start(FishState model) {
                    GearImitationAnalysis.attachGoingOutProbabilityToEveryone(model.getFishers(),
                                                                              model,
                                                                              shockSize.apply(model.getRandom()),
                                                                              explorationProbability.apply(model.getRandom()),
                                                                              imitationProbability.apply(model.getRandom()));
                }

                @Override
                public void turnOff() {

                }
            });

            state.registerStartable(adapters.get(state));
        }
        return toReturn;
    }


    public DoubleParameter getInitialProbabilityToLeavePort() {
        return initialProbabilityToLeavePort;
    }

    public void setInitialProbabilityToLeavePort(
            DoubleParameter initialProbabilityToLeavePort) {
        this.initialProbabilityToLeavePort = initialProbabilityToLeavePort;
    }

    public DoubleParameter getExplorationProbability() {
        return explorationProbability;
    }

    public void setExplorationProbability(DoubleParameter explorationProbability) {
        this.explorationProbability = explorationProbability;
    }

    public DoubleParameter getShockSize() {
        return shockSize;
    }

    public void setShockSize(DoubleParameter shockSize) {
        this.shockSize = shockSize;
    }

    public DoubleParameter getImitationProbability() {
        return imitationProbability;
    }

    public void setImitationProbability(DoubleParameter imitationProbability) {
        this.imitationProbability = imitationProbability;
    }

    public static HashMap<FishState, Startable> getAdapters() {
        return adapters;
    }

    public boolean isCheckOnlyOnceADay() {
        return checkOnlyOnceADay;
    }

    public void setCheckOnlyOnceADay(boolean checkOnlyOnceADay) {
        this.checkOnlyOnceADay = checkOnlyOnceADay;
    }
}
