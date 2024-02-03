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

import uk.ac.ox.oxfish.fisher.equipment.gear.FixedProportionGear;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

/**
 * Creates fixed proportion gears
 * Created by carrknight on 9/30/15.
 */
public class FixedProportionGearFactory implements AlgorithmFactory<FixedProportionGear> {

    /**
     * this applies to each specie
     */
    private DoubleParameter catchabilityPerHour = new FixedDoubleParameter(.01);


    public FixedProportionGearFactory() {
    }


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public FixedProportionGear apply(final FishState state) {
        return new FixedProportionGear(catchabilityPerHour.applyAsDouble(state.getRandom()));
    }

    public DoubleParameter getCatchabilityPerHour() {
        return catchabilityPerHour;
    }

    public void setCatchabilityPerHour(final DoubleParameter catchabilityPerHour) {
        this.catchabilityPerHour = catchabilityPerHour;
    }
}
