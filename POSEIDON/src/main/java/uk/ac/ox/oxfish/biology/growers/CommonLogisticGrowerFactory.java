/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

package uk.ac.ox.oxfish.biology.growers;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.UniformDoubleParameter;

public class CommonLogisticGrowerFactory implements AlgorithmFactory<CommonLogisticGrowerInitializer> {

    private DoubleParameter steepness = new FixedDoubleParameter(0.7);
    /**
     * when this is set to anything above 0, growth will be distributed with higher proportion to the area with higher
     * unfilled carrying capacity
     */
    private DoubleParameter distributionalWeight = new FixedDoubleParameter(-1);

    public CommonLogisticGrowerFactory() {
    }

    public CommonLogisticGrowerFactory(final double steepness) {
        this.steepness = new FixedDoubleParameter(steepness);
    }

    public CommonLogisticGrowerFactory(
        final double low,
        final double high
    ) {
        this.steepness = new UniformDoubleParameter(low, high);
    }

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public CommonLogisticGrowerInitializer apply(final FishState state) {
        return new CommonLogisticGrowerInitializer(
            steepness.makeCopy(),
            distributionalWeight.applyAsDouble(state.getRandom())
        );
    }

    /**
     * Getter for property 'steepness'.
     *
     * @return Value for property 'steepness'.
     */
    public DoubleParameter getSteepness() {
        return steepness;
    }

    /**
     * Setter for property 'steepness'.
     *
     * @param steepness Value to set for property 'steepness'.
     */
    public void setSteepness(final DoubleParameter steepness) {
        this.steepness = steepness;
    }

    /**
     * Getter for property 'distributionalWeight'.
     *
     * @return Value for property 'distributionalWeight'.
     */
    public DoubleParameter getDistributionalWeight() {
        return distributionalWeight;
    }

    /**
     * Setter for property 'distributionalWeight'.
     *
     * @param distributionalWeight Value to set for property 'distributionalWeight'.
     */
    public void setDistributionalWeight(final DoubleParameter distributionalWeight) {
        this.distributionalWeight = distributionalWeight;
    }

}
