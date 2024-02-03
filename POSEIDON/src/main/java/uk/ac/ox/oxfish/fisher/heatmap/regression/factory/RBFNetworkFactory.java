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

import uk.ac.ox.oxfish.fisher.heatmap.regression.basis.RBFNetworkRegression;
import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.GridXExtractor;
import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.GridYExtractor;
import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.ObservationExtractor;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

/**
 * Created by carrknight on 3/7/17.
 */
public class RBFNetworkFactory implements AlgorithmFactory<RBFNetworkRegression> {


    private final DoubleParameter learningRate = new FixedDoubleParameter(100);

    private final DoubleParameter initialWeight = new FixedDoubleParameter(1000);


    private final DoubleParameter order = new FixedDoubleParameter(5);

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public RBFNetworkRegression apply(final FishState state) {
        return new RBFNetworkRegression(

            new ObservationExtractor[]{
                new GridXExtractor(),
                new GridYExtractor()
            }, (int) order.applyAsDouble(state.getRandom()),
            new double[]{0, 0},
            new double[]{state.getMap().getWidth(), state.getMap().getHeight()},
            learningRate.applyAsDouble(state.getRandom()),
            initialWeight.applyAsDouble(state.getRandom())
        );
    }


    /**
     * Getter for property 'learningRate'.
     *
     * @return Value for property 'learningRate'.
     */
    public DoubleParameter getLearningRate() {
        return learningRate;
    }

    /**
     * Getter for property 'initialWeight'.
     *
     * @return Value for property 'initialWeight'.
     */
    public DoubleParameter getInitialWeight() {
        return initialWeight;
    }

    /**
     * Getter for property 'order'.
     *
     * @return Value for property 'order'.
     */
    public DoubleParameter getOrder() {
        return order;
    }
}
