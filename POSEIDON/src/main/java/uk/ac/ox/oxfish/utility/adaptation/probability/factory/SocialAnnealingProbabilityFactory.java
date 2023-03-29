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

package uk.ac.ox.oxfish.utility.adaptation.probability.factory;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.FishStateDailyTimeSeries;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.adaptation.probability.ThresholdExplorationProbability;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.function.Function;

/**
 * Created by carrknight on 10/17/16.
 */
public class SocialAnnealingProbabilityFactory implements AlgorithmFactory<ThresholdExplorationProbability>{


    private DoubleParameter multiplier = new FixedDoubleParameter(1);


    public SocialAnnealingProbabilityFactory() {
    }

    public SocialAnnealingProbabilityFactory(double multiplier) {

        this.multiplier = new FixedDoubleParameter(multiplier);
    }
    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public ThresholdExplorationProbability apply(FishState state) {
        return new ThresholdExplorationProbability(multiplier.applyAsDouble(state.getRandom()),
                                                   new Function<FishState, Double>() {
                                                       @Override
                                                       public Double apply(FishState model) {
                                                           return model.getLatestDailyObservation(
                                                                   FishStateDailyTimeSeries.AVERAGE_LAST_TRIP_HOURLY_PROFITS);
                                                       }
                                                   });
    }


    /**
     * Getter for property 'multiplier'.
     *
     * @return Value for property 'multiplier'.
     */
    public DoubleParameter getMultiplier() {
        return multiplier;
    }

    /**
     * Setter for property 'multiplier'.
     *
     * @param multiplier Value to set for property 'multiplier'.
     */
    public void setMultiplier(DoubleParameter multiplier) {
        this.multiplier = multiplier;
    }
}
