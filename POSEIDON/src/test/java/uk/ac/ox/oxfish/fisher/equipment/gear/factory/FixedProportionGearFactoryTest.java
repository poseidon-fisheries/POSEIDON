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

import ec.util.MersenneTwisterFast;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.fisher.equipment.gear.FixedProportionGear;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class FixedProportionGearFactoryTest {


    @Test
    public void fixedProportion() throws Exception {


        FixedProportionGearFactory fixedProportionGearFactory =
            (FixedProportionGearFactory) Gears.CONSTRUCTORS.get(
                "Fixed Proportion").get();


        fixedProportionGearFactory.setCatchabilityPerHour(new FixedDoubleParameter(.5));
        FishState state = mock(FishState.class);
        when(state.getRandom()).thenReturn(new MersenneTwisterFast());

        FixedProportionGear gear = fixedProportionGearFactory.apply(state);
        Assertions.assertEquals(gear.getProportionFished(), .5, .001);


    }
}