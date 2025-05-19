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

package uk.ac.ox.oxfish.biology.growers;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.UniformDoubleParameter;

/**
 * Created by carrknight on 1/31/17.
 */
public class SimpleLogisticGrowerFactory implements AlgorithmFactory<SimpleLogisticGrowerInitializer> {

    private DoubleParameter steepness = new FixedDoubleParameter(0.7);

    public SimpleLogisticGrowerFactory() {
    }

    public SimpleLogisticGrowerFactory(double steepness) {
        this.steepness = new FixedDoubleParameter(steepness);
    }

    public SimpleLogisticGrowerFactory(
        double low,
        double high
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
    public SimpleLogisticGrowerInitializer apply(FishState state) {
        return new SimpleLogisticGrowerInitializer(steepness.makeCopy());
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
    public void setSteepness(DoubleParameter steepness) {
        this.steepness = steepness;
    }
}
