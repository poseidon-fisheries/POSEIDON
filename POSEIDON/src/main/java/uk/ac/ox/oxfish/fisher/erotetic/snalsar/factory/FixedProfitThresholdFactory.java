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

package uk.ac.ox.oxfish.fisher.erotetic.snalsar.factory;

import uk.ac.ox.oxfish.fisher.erotetic.snalsar.FixedProfitThresholdExtractor;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

/**
 * Creates a Fixed Profit Threshold Extractor
 * Created by carrknight on 6/8/16.
 */
public class FixedProfitThresholdFactory implements AlgorithmFactory<FixedProfitThresholdExtractor> {

    /**
     * the threshold (fixed for all elements)
     */
    private DoubleParameter fixedThreshold = new FixedDoubleParameter(0d);


    public FixedProfitThresholdFactory() {
    }

    public FixedProfitThresholdFactory(final double threshold) {
        fixedThreshold = new FixedDoubleParameter(threshold);
    }

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public FixedProfitThresholdExtractor apply(final FishState state) {
        return new FixedProfitThresholdExtractor(fixedThreshold.applyAsDouble(state.getRandom()));
    }


    /**
     * Getter for property 'fixedThreshold'.
     *
     * @return Value for property 'fixedThreshold'.
     */
    public DoubleParameter getFixedThreshold() {
        return fixedThreshold;
    }

    /**
     * Setter for property 'fixedThreshold'.
     *
     * @param fixedThreshold Value to set for property 'fixedThreshold'.
     */
    public void setFixedThreshold(final DoubleParameter fixedThreshold) {
        this.fixedThreshold = fixedThreshold;
    }
}
