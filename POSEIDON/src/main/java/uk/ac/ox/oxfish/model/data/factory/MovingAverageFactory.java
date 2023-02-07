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
import uk.ac.ox.oxfish.model.data.MovingAverage;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * Builds Moving Average
 * Created by carrknight on 11/11/16.
 */
public class MovingAverageFactory implements AlgorithmFactory<MovingAverage<Double>>{


    private DoubleParameter window = new FixedDoubleParameter(20);

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public MovingAverage<Double> apply(FishState state) {
        return new MovingAverage<>(window.apply(state.getRandom()).intValue());
    }


    /**
     * Getter for property 'window'.
     *
     * @return Value for property 'window'.
     */
    public DoubleParameter getWindow() {
        return window;
    }

    /**
     * Setter for property 'window'.
     *
     * @param window Value to set for property 'window'.
     */
    public void setWindow(DoubleParameter window) {
        this.window = window;
    }
}
