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

package uk.ac.ox.oxfish.model.data.factory;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.ExponentialMovingAverage;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

/**
 * Builds EMA
 * Created by carrknight on 11/11/16.
 */
public class ExponentialMovingAverageFactory implements AlgorithmFactory<ExponentialMovingAverage<Double>> {


    private DoubleParameter alpha = new FixedDoubleParameter(.2);

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public ExponentialMovingAverage<Double> apply(final FishState state) {
        return new ExponentialMovingAverage<>(alpha.applyAsDouble(state.getRandom()));
    }


    /**
     * Getter for property 'alpha'.
     *
     * @return Value for property 'alpha'.
     */
    public DoubleParameter getAlpha() {
        return alpha;
    }

    /**
     * Setter for property 'alpha'.
     *
     * @param alpha Value to set for property 'alpha'.
     */
    public void setAlpha(final DoubleParameter alpha) {
        this.alpha = alpha;
    }
}
