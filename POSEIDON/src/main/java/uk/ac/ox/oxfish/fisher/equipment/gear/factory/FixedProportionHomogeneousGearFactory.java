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

package uk.ac.ox.oxfish.fisher.equipment.gear.factory;

import uk.ac.ox.oxfish.fisher.equipment.gear.HomogeneousAbundanceGear;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.FixedProportionFilter;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * Created by carrknight on 6/12/17.
 */
public class FixedProportionHomogeneousGearFactory implements HomogeneousGearFactory {


    private boolean rounding = true;
    private DoubleParameter catchability = new FixedDoubleParameter(.0001);

    private DoubleParameter litersOfGasConsumed = new FixedDoubleParameter(0d);

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
    public void setAverageCatchability(DoubleParameter averageCatchability) {
        this.catchability = averageCatchability;
    }

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public HomogeneousAbundanceGear apply(FishState state) {

        return new HomogeneousAbundanceGear(
                litersOfGasConsumed.apply(state.getRandom()),
                new FixedProportionFilter(getAverageCatchability().apply(state.getRandom()), rounding)
        );
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
    public void setLitersOfGasConsumed(DoubleParameter litersOfGasConsumed) {
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
    public void setRounding(boolean rounding) {
        this.rounding = rounding;
    }
}
