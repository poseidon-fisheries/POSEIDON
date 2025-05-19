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

package uk.ac.ox.oxfish.model.market.gas;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

/**
 * Created by carrknight on 7/18/17.
 */
public class FixedGasFactory implements AlgorithmFactory<FixedGasPrice> {

    private DoubleParameter gasPrice = new FixedDoubleParameter(0.01);


    public FixedGasFactory() {
    }

    public FixedGasFactory(double gasPrice) {
        this.gasPrice = new FixedDoubleParameter(gasPrice);
    }

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public FixedGasPrice apply(FishState state) {

        return new FixedGasPrice(gasPrice.applyAsDouble(state.getRandom()));

    }

    /**
     * Getter for property 'gasPrice'.
     *
     * @return Value for property 'gasPrice'.
     */
    public DoubleParameter getGasPrice() {
        return gasPrice;
    }

    /**
     * Setter for property 'gasPrice'.
     *
     * @param gasPrice Value to set for property 'gasPrice'.
     */
    public void setGasPrice(DoubleParameter gasPrice) {
        this.gasPrice = gasPrice;
    }
}
