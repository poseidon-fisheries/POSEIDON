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

package uk.ac.ox.oxfish.fisher.strategies.fishing.factory;

import uk.ac.ox.oxfish.fisher.strategies.fishing.MaximumDaysDecorator;
import uk.ac.ox.oxfish.fisher.strategies.fishing.TowLimitFishingStrategy;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * Created by carrknight on 6/21/17.
 */
public class TowLimitFactory implements AlgorithmFactory<MaximumDaysDecorator> {


    private DoubleParameter towLimits = new FixedDoubleParameter(100);


    private DoubleParameter maxDaysOut = new FixedDoubleParameter(5);


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public MaximumDaysDecorator apply(final FishState state) {
        return
            new MaximumDaysDecorator(
                new TowLimitFishingStrategy((int) towLimits.applyAsDouble(state.getRandom())),
                maxDaysOut.applyAsDouble(state.getRandom())
            );
    }

    /**
     * Getter for property 'towLimits'.
     *
     * @return Value for property 'towLimits'.
     */
    public DoubleParameter getTowLimits() {
        return towLimits;
    }

    /**
     * Setter for property 'towLimits'.
     *
     * @param towLimits Value to set for property 'towLimits'.
     */
    public void setTowLimits(final DoubleParameter towLimits) {
        this.towLimits = towLimits;
    }

    /**
     * Getter for property 'maxDaysOut'.
     *
     * @return Value for property 'maxDaysOut'.
     */
    public DoubleParameter getMaxDaysOut() {
        return maxDaysOut;
    }

    /**
     * Setter for property 'maxDaysOut'.
     *
     * @param maxDaysOut Value to set for property 'maxDaysOut'.
     */
    public void setMaxDaysOut(final DoubleParameter maxDaysOut) {
        this.maxDaysOut = maxDaysOut;
    }
}
