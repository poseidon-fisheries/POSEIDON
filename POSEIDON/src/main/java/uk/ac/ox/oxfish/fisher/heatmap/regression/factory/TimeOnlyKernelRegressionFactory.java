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

package uk.ac.ox.oxfish.fisher.heatmap.regression.factory;

import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.TimeOnlyKernelRegression;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

/**
 * Created by carrknight on 7/4/16.
 */
public class TimeOnlyKernelRegressionFactory implements AlgorithmFactory<TimeOnlyKernelRegression> {


    private DoubleParameter timeBandwidth = new FixedDoubleParameter(1000d);


    private DoubleParameter maximumNumberOfObservations = new FixedDoubleParameter(100d);


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public TimeOnlyKernelRegression apply(final FishState state) {
        return new TimeOnlyKernelRegression(
            (int) maximumNumberOfObservations.applyAsDouble(state.getRandom()),
            timeBandwidth.applyAsDouble(state.getRandom())
        );
    }

    public DoubleParameter getTimeBandwidth() {
        return timeBandwidth;
    }

    public void setTimeBandwidth(final DoubleParameter timeBandwidth) {
        this.timeBandwidth = timeBandwidth;
    }

    public DoubleParameter getMaximumNumberOfObservations() {
        return maximumNumberOfObservations;
    }

    public void setMaximumNumberOfObservations(final DoubleParameter maximumNumberOfObservations) {
        this.maximumNumberOfObservations = maximumNumberOfObservations;
    }
}
