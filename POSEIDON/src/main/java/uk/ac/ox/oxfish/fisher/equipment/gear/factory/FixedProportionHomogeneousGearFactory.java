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

package uk.ac.ox.oxfish.fisher.equipment.gear.factory;

import uk.ac.ox.oxfish.fisher.equipment.gear.HomogeneousAbundanceGear;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.FixedProportionFilter;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

/**
 * Created by carrknight on 6/12/17.
 */
public class FixedProportionHomogeneousGearFactory implements HomogeneousGearFactory {


    private boolean rounding = true;
    private DoubleParameter catchability = new FixedDoubleParameter(.0001);

    private DoubleParameter litersOfGasConsumed = new FixedDoubleParameter(0d);

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public HomogeneousAbundanceGear apply(final FishState state) {

        return new HomogeneousAbundanceGear(
            litersOfGasConsumed.applyAsDouble(state.getRandom()),
            new FixedProportionFilter(getAverageCatchability().applyAsDouble(state.getRandom()), rounding)
        );
    }

    /**
     * Getter for property 'averageCatchability'.
     *
     * @return Value for property 'averageCatchability'.
     */
    @Override
    public DoubleParameter getAverageCatchability() {
        return catchability;
    }

    /**
     * Setter for property 'averageCatchability'.
     *
     * @param averageCatchability Value to set for property 'averageCatchability'.
     */
    @Override
    public void setAverageCatchability(final DoubleParameter averageCatchability) {
        this.catchability = averageCatchability;
    }

    /**
     * Getter for property 'litersOfGasConsumed'.
     *
     * @return Value for property 'litersOfGasConsumed'.
     */
    public DoubleParameter getLitersOfGasConsumed() {
        return litersOfGasConsumed;
    }

    /**
     * Setter for property 'litersOfGasConsumed'.
     *
     * @param litersOfGasConsumed Value to set for property 'litersOfGasConsumed'.
     */
    public void setLitersOfGasConsumed(final DoubleParameter litersOfGasConsumed) {
        this.litersOfGasConsumed = litersOfGasConsumed;
    }


    /**
     * Getter for property 'rounding'.
     *
     * @return Value for property 'rounding'.
     */
    public boolean isRounding() {
        return rounding;
    }

    /**
     * Setter for property 'rounding'.
     *
     * @param rounding Value to set for property 'rounding'.
     */
    public void setRounding(final boolean rounding) {
        this.rounding = rounding;
    }
}
