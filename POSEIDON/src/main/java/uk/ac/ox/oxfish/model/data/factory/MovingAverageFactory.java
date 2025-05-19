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

package uk.ac.ox.oxfish.model.data.factory;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.MovingAverage;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

/**
 * Builds Moving Average
 * Created by carrknight on 11/11/16.
 */
public class MovingAverageFactory implements AlgorithmFactory<MovingAverage<Double>> {


    private DoubleParameter window = new FixedDoubleParameter(20);

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public MovingAverage<Double> apply(final FishState state) {
        return new MovingAverage<>((int) window.applyAsDouble(state.getRandom()));
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
    public void setWindow(final DoubleParameter window) {
        this.window = window;
    }
}
