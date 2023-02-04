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

package uk.ac.ox.oxfish.fisher.heatmap.regression.factory;

import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.TimeOnlyKernelRegression;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * Created by carrknight on 7/4/16.
 */
public class TimeOnlyKernelRegressionFactory implements AlgorithmFactory<TimeOnlyKernelRegression> {


    private DoubleParameter timeBandwidth = new FixedDoubleParameter(1000d);


    private DoubleParameter maximumNumberOfObservations= new FixedDoubleParameter(100d);



    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public TimeOnlyKernelRegression apply(FishState state) {
        return new TimeOnlyKernelRegression(
                maximumNumberOfObservations.apply(state.getRandom()).intValue(),
                timeBandwidth.apply(state.getRandom())
        );
    }

    public DoubleParameter getTimeBandwidth() {
        return timeBandwidth;
    }

    public void setTimeBandwidth(DoubleParameter timeBandwidth) {
        this.timeBandwidth = timeBandwidth;
    }

    public DoubleParameter getMaximumNumberOfObservations() {
        return maximumNumberOfObservations;
    }

    public void setMaximumNumberOfObservations(DoubleParameter maximumNumberOfObservations) {
        this.maximumNumberOfObservations = maximumNumberOfObservations;
    }
}
