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

package uk.ac.ox.oxfish.fisher.equipment.gear.factory;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.fisher.equipment.gear.SelectivityAbundanceGear;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

public class LogisticSelectivityGearFactoryTest {


    @Test
    public void create() {

        LogisticSelectivityGearFactory factory = new LogisticSelectivityGearFactory();
        factory.setAverageCatchability(new FixedDoubleParameter(.123));
        factory.setSelectivityAParameter(new FixedDoubleParameter(.234));
        factory.setSelectivityBParameter(new FixedDoubleParameter(.345));


        SelectivityAbundanceGear gear = (SelectivityAbundanceGear) factory.apply(new FishState());
        Assertions.assertEquals(gear.getaParameter(), .234, .0001);
        Assertions.assertEquals(gear.getbParameter(), .345, .0001);

    }
}
