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

package uk.ac.ox.oxfish.fisher.strategies.weather.factory;

import uk.ac.ox.oxfish.fisher.strategies.weather.WindThresholdStrategy;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.NormalDoubleParameter;

/**
 * Creates WindThreshold strategies
 * Created by carrknight on 9/8/15.
 */
public class WindThresholdFactory  implements AlgorithmFactory<WindThresholdStrategy> {



    private DoubleParameter maximumWindSpeedTolerated = new NormalDoubleParameter(50,10);


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public WindThresholdStrategy apply(FishState state) {
        return new WindThresholdStrategy(maximumWindSpeedTolerated.apply(state.getRandom()));
    }

    public DoubleParameter getMaximumWindSpeedTolerated() {
        return maximumWindSpeedTolerated;
    }

    public void setMaximumWindSpeedTolerated(DoubleParameter maximumWindSpeedTolerated) {
        this.maximumWindSpeedTolerated = maximumWindSpeedTolerated;
    }
}
